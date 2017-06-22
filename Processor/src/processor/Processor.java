package processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class Register{
    public int value = 0;
    public boolean status = false;
    public int qi = -1;
    public boolean busy = false;
    public int reorder = 0;
}

class ReservationStation{
    public String name;
    public boolean busy = false;
    public Operation op = Operation.EMPTY;    
    public int dest = -1;
    public int vj = -1;
    public int vk = -1;
    public int qj = -1;
    public int qk = -1;
    public int A = -1;
    
    public ReservationStation(String name){
        this.name = name;
    }
}

class ReorderBuffer{
    public boolean busy = false;
    public String instruction = "";
    public State state = State.ISSUE;
    public boolean ready = false;
    public int destination = -1;
    public String destinationType = null; //se é Registrador, memoria...
    public int value = -1;
}

public class Processor {
    //Constantes
    private final int  N_Register = 32;
    private final int N_Reservation_Add = 3;
    private final int N_Reservation_Mult = 3;
    private final int N_Reservation_Mem = 5;
    private final int N_Memorias_Var = 1000;
    
    private int pc = 0;
    private int clock = 0;
    private int instructionCounter = 0;
    
    private List<Integer> memoriaVariaveis = Arrays.asList(new Integer[N_Memorias_Var]);
    private List<Register> r = Arrays.asList(new Register[N_Register]);
    
    private final List<Command> commands = new ArrayList<>();
    
    //OBS: cuidado se remover algm do rob, vai ter que ajustar indices nos em r[] e nas estacoes
    private final List<ReorderBuffer> rob = new ArrayList<>();
    private final List<Command> filaDeInstrucoes = new ArrayList<>();
    private final List<ReservationStation> reservationStationsSoma = Arrays.asList(new ReservationStation[N_Reservation_Add]);
    private final List<ReservationStation> reservationStationsMultiplicacao = Arrays.asList(new ReservationStation[N_Reservation_Mult]);
    private final List<ReservationStation> reservationStationsMemoria = Arrays.asList(new ReservationStation[N_Reservation_Mem]);
    
    //variaveis temporarias, necessarias para simular paralelismo
    private List<Integer> memoriaVariaveisTemp = Arrays.asList(new Integer[N_Memorias_Var]);
    private final List<Register> rTemp = Arrays.asList(new Register[N_Register]);
    private final List<ReorderBuffer> robTemp = new ArrayList<>();
    
    public Processor(){
        initEstacoesReservaERob();
        readFile();
    }
    
    private void readFile(){
        Command.setMap();
        String filename = "program.txt";
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {                
                commands.add(Command.parseCommand(line.split(";")[0],line.split(";")[1]));
            }
        }catch(IOException e){
            System.out.println("Erro na leitura");
        }
    }
    
    private void initEstacoesReservaERob(){
        for(int i = 0;i<reservationStationsSoma.size();i++){
            reservationStationsSoma.set(i,new ReservationStation("Add"));
        }
        for(int i = 0;i<reservationStationsMultiplicacao.size();i++){
            reservationStationsMultiplicacao.set(i,new ReservationStation("Mult"));
        }
        for(int i = 0;i<reservationStationsMemoria.size();i++){
            reservationStationsMemoria.set(i,new ReservationStation("Load/Store"));
        }
        for(int i = 0;i<10;i++){
            rob.add(new ReorderBuffer());
            robTemp.add(new ReorderBuffer()); 
        }
        for (int i=0;i<r.size();i++){
            r.add(new Register());
        }
    }
    
    public void nextClock(){
        process();
    }
    
    //processador principal
    private void process(){
        issue();
        //execute(); //TO DO
        //write(); //TO DO
        //commit(); //TO DO
        
        //ATUALIZAR PC,CLOCK
        pc = pc + 4;
        clock++;
    }
    
    public void issue(){
        //colocar da memoria na fila
        if (pc/4 < commands.size()){
            filaDeInstrucoes.add(commands.get(pc/4)); //tem que ser pc/4
            instructionCounter++;
        }
        //TO DO : verificar se é um jump condicional e fazer a predicao
        
        //da fila pra estacao de reserva e o rob        
        boolean removeuDaFila = false; //gambiarra
        if(!filaDeInstrucoes.isEmpty()){
            Command co = filaDeInstrucoes.get(0);
            //encontrar a estacao de reserva correspondente
            List<ReservationStation> res = null;
            if (co.isEstacaoMem()){
                res = reservationStationsMemoria;
            }
            if(co.isEstacaoMult()){
                res = reservationStationsMultiplicacao; 
            }
            if(co.isEstacaoSoma()){
               res = reservationStationsSoma;
            }
            for(int i = 0;i<res.size();i++){
                if (res.get(i).busy)continue;
                
                removeuDaFila = true;
                //encontrar um rob nao ocupado
                int b = 30;
                for (int j = 0;j<rob.size();j++){
                    if(!rob.get(j).busy)
                        b = j;
                }
                //tem operandos rs e rt
                if (co.commandType == CommandType.R || co.op == Operation.BEQ
                        || co.op == Operation.BLE || co.op == Operation.BNE || co.op == Operation.LW){
                    //se alguma instrucao grava em rs
                    if(r.get(co.rs).busy){
                        int h = r.get(co.rs).reorder;
                        if(rob.get(h).ready){//inst ja concluida
                            res.get(i).vj = rob.get(h).value;
                            res.get(i).qj = 0;
                        }
                        else
                            res.get(i).qj = h;                            
                    }                        
                    else{
                        res.get(i).vj = r.get(co.rs).value;
                        res.get(i).qj = 0;
                    }
                    res.get(i).busy = true;
                    res.get(i).dest = b;
                    robTemp.get(b).instruction = co.name;
                    robTemp.get(b).ready = false;     
                    //se alguma instrucao grava em rt
                    if(r.get(co.rt).busy){
                        int h = r.get(co.rt).reorder;
                        if(rob.get(h).ready){//inst ja concluida
                            res.get(i).vk = rob.get(h).value;
                            res.get(i).qk = -1;
                        }
                        else
                            res.get(i).qk = h; 
                    }
                    else{
                        res.get(i).vk = r.get(co.rt).value;
                        res.get(i).qk = -1;
                    }
                }
                //INTRUCAO R: R[rd] = R[rs] op R[rt]
                if(co.commandType == CommandType.R){   
                    //grava em rd
                    robTemp.get(b).destination = co.rd;
                    rTemp[co.rd].qi = b;
                    rTemp[co.rd].busy = true;
                }
                //caso addi rt = rs + imm
                //load r[rt] = MEM[r[rs] + imm]]
                if(co.op == Operation.ADDI || co.op == Operation.LW){
                    if(r[co.rs].busy){
                        int h = r[co.rs].reorder;
                        if(rob.get(h).ready){//inst ja concluida
                            res.get(i).vj = rob.get(h).value;
                            res.get(i).qj = -1;
                        }
                        else
                            res.get(i).qj = h;                            
                    }                        
                    else{
                        res.get(i).vj = r[co.rs].value;
                        res.get(i).qj = -1;
                    }                        
                    res.get(i).busy = true;
                    res.get(i).dest = b;
                    robTemp.get(b).instruction = co.name;
                    robTemp.get(b).destination = co.rt;
                    robTemp.get(b).ready = false; 
                    //immediate
                    res.get(i).vk = co.immediate;
                    res.get(i).qk = 0;
                    //rt
                    rTemp[co.rt].qi = b;
                    rTemp[co.rt].busy = true;
                    if(co.op == Operation.LW){
                        res.get(i).A = co.immediate;
                    }
                }
                if(co.op == Operation.SW || co.op == Operation.LW){                        
                    res.get(i).A = co.immediate;
                }
            }
        }
        
        if(removeuDaFila){
            filaDeInstrucoes.remove(0);
        }
    }   
    
    public int getPc() {
        return pc;
    }

    public int getClock() {
        return clock;
    }
    
    public int getInstructionCounter(){
        return instructionCounter;
    }

    public List<Command> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    public List<ReorderBuffer> getRo() {
        return Collections.unmodifiableList(rob);
    }

    public List<Command> getFilaDeInstrucoes() {
        return Collections.unmodifiableList(filaDeInstrucoes);
    }

    public List getReservationStationsSoma() {
        return Collections.unmodifiableList(reservationStationsSoma);
    }

    public List getReservationStationsMultiplicacao() {
        return Collections.unmodifiableList(reservationStationsMultiplicacao);
    }

    public List getReservationStationsMemoria() {
        return Collections.unmodifiableList(reservationStationsMemoria);
    }

    public List getRegisterStatus() {
        return Collections.unmodifiableList(Arrays.asList(r));
    }
}