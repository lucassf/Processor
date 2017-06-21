package processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class Register{
    public int value = 0;
    public boolean status;
    public int qi = -1;
    public boolean busy = false;
    public int reorder = 0;
    
}

class ReservationStation{
    public String name;
    public boolean busy;
    public Operation op;    
    public int dest;
    public int vj;
    public int vk;
    public int qj;
    public int qk;
    public int A;
}

class ReorderBuffer{
    public boolean busy = false;
    public String instruction;
    public State state;
    public boolean ready = false;
    public int destination;
    public String destinationType; //se é Registrador, memoria...
    public int value;
}

public class Processor {
    private final int  N_Register = 32;
    private int pc = 0;
    private int clock = 0;
    private int instructionCounter = 0;
    private int[] memoriaVariaveis = new int[1000];
    private Register[] r = new Register[N_Register];
    private Vector<Command> commands = new Vector();
    //OBS: cuidado se remover algm do rob, vai ter que ajustar indices nos em r[] e nas estacoes
    private ArrayList<ReorderBuffer> rob = new ArrayList<>();
    private ArrayList<Command> filaDeInstrucoes = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsSoma = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsMultiplicacao = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsMemoria = new ArrayList<>();
    
    //variaveis temporarias, necessarias para simular paralelismo
    private int[] memoriaVariaveisTemp = new int[1000];
    private Register[] rTemp = new Register[N_Register];
    private ArrayList<ReorderBuffer> robTemp = new ArrayList<>();
    private ArrayList<Command> filaDeInstrucoesTemp = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsSomaTemp = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsMultiplicacaoTemp = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsMemoriaTemp = new ArrayList<>();
    
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
        for(int i = 0;i<3;i++){
            ReservationStation temp = new ReservationStation();
            temp.busy = false;
            temp.name = "Add";
            reservationStationsSoma.add(temp); 
            temp = new ReservationStation();
            temp.busy = false;
            temp.name = "Add";
            reservationStationsSomaTemp.add(temp); 
        }
        for(int i = 0;i<2;i++){
            ReservationStation temp = new ReservationStation();
            temp.busy = false;
            temp.name = "Mult";
            reservationStationsMultiplicacao.add(temp); 
            temp = new ReservationStation();
            temp.busy = false;
            temp.name = "Mult";
            reservationStationsMultiplicacaoTemp.add(temp); 
        }
        for(int i = 0;i<5;i++){
            ReservationStation temp = new ReservationStation();
            temp.busy = false;
            temp.name = "Load/Store";
            reservationStationsMemoria.add(temp); 
            temp = new ReservationStation();
            temp.busy = false;
            temp.name = "Load/Store";
            reservationStationsMemoriaTemp.add(temp); 
        }
        for(int i = 0;i<10;i++){
            ReorderBuffer temp = new ReorderBuffer();
            temp.busy = false;
            rob.add(temp); 
            temp = new ReorderBuffer();
            temp.busy = false;
            robTemp.add(temp); 
        }
            
    }
    public Processor(){
        initEstacoesReservaERob();
        readFile();
    }
    public void issue(){
        //colocar da memoria na fila
        if (pc/4 < commands.size()){
            filaDeInstrucoesTemp.add(commands.get(pc/4)); //tem que ser pc/4
            instructionCounter++;
        }
        //TO DO : verificar se é um jump condicional e fazer a predicao
        
        //da fila pra estacao de reserva e o rob        
        boolean removeuDaFila = false; //gambiarra
        if(!filaDeInstrucoes.isEmpty()){
            Command co = filaDeInstrucoes.get(0);
            //encontrar a estacao de reserva correspondente
            ArrayList<ReservationStation> res = null;
            int size = 0;
            if (co.isEstacaoMem()){
                res = reservationStationsMemoriaTemp;
                size = 5;
            }
            if(co.isEstacaoMult()){
                res = reservationStationsMultiplicacaoTemp;     
                size = 2;
            }
            if(co.isEstacaoSoma()){
               res = reservationStationsSomaTemp;
               size = 3;
            }
            for(int i = 0;i<size;i++){
                if(!res.get(i).busy){
                    removeuDaFila = true;
                    //encontar um rob nao ocupado
                    int b = 30;
                    for (int j = 0;j<10;j++){
                        if(!rob.get(j).busy)
                            b = j;
                    }
                    //tem operandos rs e rt
                    if (co.commandType == CommandType.R || co.op == Operation.BEQ
                            || co.op == Operation.BLE || co.op == Operation.BNE || co.op == Operation.LW){
                        //se alguma instrucao grava em rs
                        if(r[co.rs].busy){
                            int h = r[co.rs].reorder;
                            if(rob.get(h).ready){//inst ja concluida
                                res.get(i).vj = rob.get(h).value;
                                res.get(i).qj = 0;
                            }
                            else
                                res.get(i).qj = h;                            
                        }                        
                        else{
                            res.get(i).vj = r[co.rs].value;
                            res.get(i).qj = 0;
                        }
                        res.get(i).busy = true;
                        res.get(i).dest = b;
                        robTemp.get(b).instruction = co.name;
                        robTemp.get(b).ready = false;     
                        //se alguma instrucao grava em rt
                        if(r[co.rt].busy){
                            int h = r[co.rt].reorder;
                            if(rob.get(h).ready){//inst ja concluida
                                res.get(i).vk = rob.get(h).value;
                                res.get(i).qk = -1;
                            }
                            else
                                res.get(i).qk = h; 
                        }
                        else{
                            res.get(i).vk = r[co.rt].value;
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
                    
                    
                    break;
                }
            }
        }        
        Collections.copy(filaDeInstrucoes, filaDeInstrucoesTemp);
        if(removeuDaFila){
            filaDeInstrucoes.remove(0);
            filaDeInstrucoesTemp.remove(0);
        }
        //ATUALIZAR PC
        pc = pc + 4;
    }    
    //processador principal
    public void process(){
        //fazer tudo dentro de um loop até acabar!!!!
        issue();
        execute(); //TO DO
        write(); //TO DO
        commit(); //TO DO
        Collections.copy(rob, robTemp);
        Collections.copy(reservationStationsMemoria, reservationStationsMemoriaTemp);
        Collections.copy(reservationStationsMultiplicacao, reservationStationsMultiplicacaoTemp);
        Collections.copy(reservationStationsSoma, reservationStationsSomaTemp);
        memoriaVariaveis = Arrays.copyOf(memoriaVariaveisTemp, memoriaVariaveisTemp.length);
        r = Arrays.copyOf(rTemp, rTemp.length);
        
    }
    /*
    public void issue (Command New_Instruction, int Instruction_index)
    {
        int i;
        for (i = 0; i < N_ReservationStation; i++)
            if (!reservationStations[i].busy)
                break;
        
        if (i < N_ReservationStation)    //Existe uma estação de reserva vazia.
        {
            reservationStations[i].busy = true;
            New_Instruction.T_Dest = i;
            if (New_Instruction.isR())
            {
                reservationStations[i].qj = FindCorrespondingSource(New_Instruction.rs, Instruction_index);
                reservationStations[i].qk = FindCorrespondingSource(New_Instruction.rt, Instruction_index);
            }
            
            if (New_Instruction.isI())
            {
                reservationStations[i].qj = FindCorrespondingSource(New_Instruction.rd, Instruction_index);
            }
            
        }
       
    }*/
    
    //Percorre todos os índices de 0 a Instruction_index - 1, procurando a instrução com maior índice
    //de modo que Source_Register seja o registrador de destino, e retorna o índice da estação de reserva
    //correspondente.
    /*public int FindCorrespondingSource (int Source_Register, int Instruction_index)
    {
        for (int i = Instruction_index - 1; i >= 0; i++)
            if (commands.get(i).rd == Source_Register)
                return commands.get(i).T_Dest;
        return Source_Register;    
    }*/
    
    public void nextClock(){
        
    }
    
    
}
