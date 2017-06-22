package processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Arrays;
import java.util.List;

class Register{
    public int value = 0;
    public boolean status;
    public int qi = -1; 
    public boolean busy = false;
    public int reorder = -1;
    
    public Register(Register r){
        this.value = r.value;
        this.status = r.status;
        this.qi = r.qi;
        this.busy = r.busy;
        this.reorder = r.reorder;
    }
    public Register(){
        this.value = 0;
        this.qi = -1;
        this.busy = false;
        this.reorder = -1;
    }
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
    
    public ReservationStation(ReservationStation r){
        this.name = r.name;
        this.busy = r.busy;
        this.op = r.op;
        this.dest = r.dest;
        this.vj = r.vj;
        this.vk = r.vk;
        this.qj = r.qj;
        this.qk = r.qk;
        this.A = r.A;
    }
    public ReservationStation(){
        name = "";
        busy = false;
        op = Operation.EMPTY;
        dest = vj = vk = qj = qk = A = -1;
    }
}
class ULA{
    public boolean busy = false;
    public boolean done = false;
    public int result = 0;
    public int contClocks = 0;
    public int timeToFinish;
    public ReservationStation rStationOperando;
    
    public ULA(int i){
        this.timeToFinish = i;
    }
    public int doFPOperation(){
        if(rStationOperando.op == Operation.ADD || rStationOperando.op == Operation.ADDI)
            return rStationOperando.vj + rStationOperando.vk;
        if(rStationOperando.op == Operation.MUL)
            return rStationOperando.vj * rStationOperando.vk;
        if(rStationOperando.op == Operation.SUB)
            return rStationOperando.vj - rStationOperando.vk;
        if(rStationOperando.op == Operation.BNE)
            return rStationOperando.vj - rStationOperando.vk;
        if(rStationOperando.op == Operation.SUB)
            return rStationOperando.vj - rStationOperando.vk;
        if(rStationOperando.op == Operation.SUB)
            return rStationOperando.vj - rStationOperando.vk;
        return 0; //para as instrucoes que nao tem retorno
    }
}
class ReorderBuffer{
    public boolean busy;
    public String instruction;
    public State state;
    public boolean ready;
    public int destination;
    public String destinationType; //se é Registrador, memoria...
    public int value;
    
    public ReorderBuffer(ReorderBuffer r){
        this.busy = r.busy;
        this.instruction = r.instruction;
        this.state = r.state;
        this.ready = r.ready;
        this.destination = r.destination;
        this.destinationType = r.destinationType;
        this.value = r.value;
    }
    public ReorderBuffer(){
        this.busy = false;
        this.ready = false;
    }
}

public class Processor {
    private final int  N_Register = 32;
    private final int N_Reservation_Soma = 3;
    private final int N_Reservation_Mult = 3;
    private final int N_Reservation_Mem = 5;
    
    private ULA ulaAdd = new ULA(1);
    private ULA ulaMult= new ULA(3);
    private ULA ulaMem = new ULA(4);
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
    
    public void nextClock(){
        process();
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
    private void initEstacoesReservaERobERegister(){
        for(int i = 0;i<N_Reservation_Soma;i++){
            reservationStationsSoma.add(new ReservationStation());
            reservationStationsSomaTemp.add(new ReservationStation());
        }
        for(int i = 0;i<N_Reservation_Mult;i++){
            reservationStationsMultiplicacao.add(new ReservationStation());
            reservationStationsMultiplicacaoTemp.add(new ReservationStation()); 
        }
        for(int i = 0;i<N_Reservation_Mem;i++){
            reservationStationsMemoria.add(new ReservationStation());
            reservationStationsMemoriaTemp.add(new ReservationStation()); 
        }
        for(int i = 0;i<10;i++){
            rob.add(new ReorderBuffer());
            robTemp.add(new ReorderBuffer()); 
        }
        for(int i = 0;i<N_Register;i++){
            Register temp = new Register();
            r[i] = temp;
            rTemp[i] = temp;
        }
            
    }
    public Processor(){
        initEstacoesReservaERobERegister();
        readFile();
    }
    public void issue(){
        //colocar da memoria na fila
        if (getPc()/4 < commands.size()){
            filaDeInstrucoesTemp.add(commands.get(getPc()/4)); //tem que ser pc/4
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
        filaDeInstrucoes = new ArrayList<>(filaDeInstrucoesTemp);
        if(removeuDaFila){
            filaDeInstrucoes.remove(0);
            filaDeInstrucoesTemp.remove(0);
        }
        //ATUALIZAR PC
        pc = getPc() + 4;
    }    
    //TO DO
    public void execute(){
        //add
        if(ulaAdd.busy){
            if(ulaAdd.contClocks == ulaAdd.timeToFinish){
                ulaAdd.doFPOperation();
                //FALTA OS BRANCHES
            }
        }
        else{
            
        }
        if(ulaMem.busy){
            
        }
        else{
            
        }
        if(ulaMult.busy){
            
        }
        else{
            
        }
    }
    //processador principal
   
    public void process(){
        //fazer tudo dentro de um loop até acabar!!!!
        issue();
        execute(); //TO DO
    //    write(); //TO DO
    //    commit(); //TO DO
        
        //OBS: deve ser necessario copiar objeto a objeto, nao a lista  
        ArrayList<ReorderBuffer> rob2 = new ArrayList<>();
        for (ReorderBuffer r : robTemp){
            rob2.add(new ReorderBuffer(r));
        }
        rob = new ArrayList<>(rob2);
        ArrayList<ReservationStation> r2 = new ArrayList<>();
        for (ReservationStation r: reservationStationsMemoriaTemp){
            r2.add(new ReservationStation(r));
        }
        reservationStationsMemoria = new ArrayList<>(r2);
        r2.clear();
        for (ReservationStation r: reservationStationsMultiplicacaoTemp){
            r2.add(new ReservationStation(r));
        }
        reservationStationsMultiplicacao = new ArrayList<>(r2);        
        r2.clear();
        for (ReservationStation r: reservationStationsSomaTemp){
            r2.add(new ReservationStation(r));
        }
        reservationStationsSoma = new ArrayList<>(r2);    
        r2.clear();
        Register[] r3 = new Register[N_Register];
        for(int i = 0;i<N_Register;i++)
            r[i] = new Register(rTemp[i]);
        memoriaVariaveis = Arrays.copyOf(memoriaVariaveisTemp, memoriaVariaveisTemp.length);
        
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
    
    public ArrayList<ReorderBuffer> getRob() {
        return rob;
    }

    public ArrayList<ReservationStation> getReservationStationsSoma() {
        return reservationStationsSoma;
    }

    public ArrayList<ReservationStation> getReservationStationsMultiplicacao() {
        return reservationStationsMultiplicacao;
    }

    public ArrayList<ReservationStation> getReservationStationsMemoria() {
        return reservationStationsMemoria;
    }

    public int getPc() {
        return pc;
    }

    public int getClock() {
        return clock;
    }

    public int getInstructionCounter() {
        return instructionCounter;
    }

    public List<Register> getR() {
        return Arrays.asList(r);
    }
}