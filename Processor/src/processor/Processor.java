package processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Arrays;
import java.util.List;

class Register {

    public int value = 0;
    public boolean status;
    public int qi = -1;
    public boolean busy = false;

    public Register(Register r) {
        this.value = r.value;
        this.status = r.status;
        this.qi = r.qi;
        this.busy = r.busy;
    }

    public Register() {
        this.value = 0;
        this.qi = -1;
        this.busy = false;
    }
}

class ReservationStation {

    public String name;
    public boolean busy;
    public Operation op;
    public int dest;
    public int vj;
    public int vk;
    public int qj;
    public int qk;
    public int A;

    public ReservationStation(ReservationStation r) {
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

    public ReservationStation() {
        name = "";
        busy = false;
        op = Operation.EMPTY;
        dest = vj = vk = qj = qk = A = -1;
    }
}

class ULA {

    public boolean busy = false;
    public boolean done = false;
    public int result = -1;// branch: 0 se nao jump, 1 se jump
    public int contClocks = 0;
    public int timeToFinish;
    public int vj = -1;
    public int vk = -1; 
    public int A = -1;
    public int stationIndex = -1;//estacao de reserva em execucao
    public Operation op;
   // public ReservationStation rStationOperando = null;

    public ULA(int i) {
        this.timeToFinish = i;
    }

    public void doFPOperation() {
        if (op == Operation.ADD || op == Operation.ADDI) {
            result = vj + vk;
        }
        if (op == Operation.MUL) {
            result = vj * vk;
        }
        if (op == Operation.SUB) {
            result = vj - vk;
        }
        if (op == Operation.BNE) {
            if (vk != vj)
                result = 1;
            else
                result = 0;
        }
        if (op == Operation.BLE ) {
            if (vj<=vk)
                result = 1;
            else
                result = 0;
        }
        if (op == Operation.BEQ ) {
            if (vj==vk)
                result = 1;
            else
                result = 0;
        }
    }
    public void doSwOperation(){
        result = vj + A;
    }
}

class ReorderBuffer {

    public boolean busy;
    public String instruction;
    public State state;
    public boolean ready;
    public int destination;
    public String destinationType; //se é Registrador, memoria...
    public int value;
    public int address; //usado nos branches e store

    public ReorderBuffer(ReorderBuffer r) {
        this.busy = r.busy;
        this.instruction = r.instruction;
        this.state = r.state;
        this.ready = r.ready;
        this.destination = r.destination;
        this.destinationType = r.destinationType;
        this.value = r.value;
        this.address = r.address;
    }

    public ReorderBuffer() {
        this.busy = false;
        instruction = "";
        state = State.ISSUE;
        this.ready = false;
        destination = -1;
        destinationType = "";
        value = -1;
    }
}

public class Processor {

    private final int N_Register = 32;
    private final int N_Reservation_Soma = 3;
    private final int N_Reservation_Mult = 2;
    private final int N_Reservation_Mem = 5;

    private ULA ulaAdd = new ULA(1);
    private ULA ulaMult = new ULA(3);
    private ULA ulaMem = new ULA(4);
    private int pc = 0;
    private int clock = 0;
    private int instructionCounter = 0;
    private int[] memoriaVariaveis = new int[4000];
    private Register[] regs = new Register[N_Register];
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

    public void nextClock() {
        process();
    }

    private void readFile() {
        Command.setMap();
        String filename = "program.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                commands.add(Command.parseCommand(line.split(";")[0], line.split(";")[1]));
            }
        } catch (IOException e) {
            System.out.println("Erro na leitura");
        }
    }

    private void initEstacoesReservaERobERegister() {
        for (int i = 0; i < N_Reservation_Soma; i++) {
            reservationStationsSoma.add(new ReservationStation());
            reservationStationsSomaTemp.add(new ReservationStation());
        }
        for (int i = 0; i < N_Reservation_Mult; i++) {
            reservationStationsMultiplicacao.add(new ReservationStation());
            reservationStationsMultiplicacaoTemp.add(new ReservationStation());
        }
        for (int i = 0; i < N_Reservation_Mem; i++) {
            reservationStationsMemoria.add(new ReservationStation());
            reservationStationsMemoriaTemp.add(new ReservationStation());
        }
        for (int i = 0; i < 10; i++) {
            rob.add(new ReorderBuffer());
            robTemp.add(new ReorderBuffer());
        }
        for (int i = 0; i < N_Register; i++) {
            Register temp = new Register();
            regs[i] = temp;
            rTemp[i] = temp;
        }

    }

    public Processor() {
        initEstacoesReservaERobERegister();
        readFile();
    }

    public void issue() {
        //colocar da memoria na fila
        if (pc / 4 < commands.size()) {
            filaDeInstrucoesTemp.add(commands.get(pc / 4)); //tem que ser pc/4
            instructionCounter++;
        }
        //TO DO : verificar se é um jump condicional e fazer a predicao

        //da fila pra estacao de reserva e o rob        
        boolean removeuDaFila = false; //gambiarra
        if (!filaDeInstrucoes.isEmpty()) {
            Command co = filaDeInstrucoes.get(0);
            //encontrar a estacao de reserva correspondente
            ArrayList<ReservationStation> rs;
            if (co.isEstacaoMem()) {
                rs = reservationStationsMemoriaTemp;
            }
            else if (co.isEstacaoMult()) {
                rs = reservationStationsMultiplicacaoTemp;
            }
            else {
                rs = reservationStationsSomaTemp;
            }
            for (int r = 0; r < rs.size(); r++) {
                if (rs.get(r).busy) {
                    continue;
                }
                removeuDaFila = true;
                //encontar um rob nao ocupado
                int b = 30;
                for (int j = 0; j < rob.size(); j++) {
                    if (!rob.get(j).busy) {
                        b = j;
                        break;
                    }
                }
                rs.get(r).name = co.name;
                //tem operandos rs e rt
                if (co.commandType == CommandType.R || co.op == Operation.BEQ
                        || co.op == Operation.BLE || co.op == Operation.BNE || co.op == Operation.LW) {
                    //se alguma instrucao grava em rs
                    if (regs[co.rs].busy) {
                        int h = regs[co.rs].qi;
                        if (rob.get(h).ready) {//inst ja concluida
                            rs.get(r).vj = rob.get(h).value;
                            rs.get(r).qj = 0;
                        } else {
                            rs.get(r).qj = h;
                        }
                    } else {
                        rs.get(r).vj = regs[co.rs].value;
                        rs.get(r).qj = 0;
                    }
                    rs.get(r).busy = true;
                    rs.get(r).dest = b;
                    robTemp.get(b).instruction = co.name;
                    robTemp.get(b).destination = co.rd;
                    robTemp.get(b).ready = false;
                    //se alguma instrucao grava em rt
                    if (regs[co.rt].busy) {
                        int h = regs[co.rt].qi;
                        if (rob.get(h).ready) {//inst ja concluida
                            rs.get(r).vk = rob.get(h).value;
                            rs.get(r).qk = 0;
                        } else {
                            rs.get(r).qk = h;
                        }
                    } else {
                        rs.get(r).vk = regs[co.rt].value;
                        rs.get(r).qk = -1;
                    }
                }
                //INTRUCAO R: R[rd] = R[rs] op R[rt]
                if (co.commandType == CommandType.R) {
                    //grava em rd
                    robTemp.get(b).destination = co.rd;
                    rTemp[co.rd].qi = b;
                    rTemp[co.rd].busy = true;
                }
                //caso addi rt = rs + imm
                //load r[rt] = MEM[r[rs] + imm]]
                if (co.op == Operation.ADDI || co.op == Operation.LW) {
                    if (regs[co.rs].busy) {
                        int h = regs[co.rs].qi;
                        if (rob.get(h).ready) {//inst ja concluida
                            rs.get(r).vj = rob.get(h).value;
                            rs.get(r).qj = 0;
                        } else {
                            rs.get(r).qj = h;
                        }
                    } else {
                        rs.get(r).vj = regs[co.rs].value;
                        rs.get(r).qj = 0;
                    }
                    rs.get(r).busy = true;
                    rs.get(r).dest = b;
                    robTemp.get(b).instruction = co.name;
                    robTemp.get(b).destination = co.rt;
                    robTemp.get(b).ready = false;
                    //immediate
                    rs.get(r).vk = co.immediate;
                    rs.get(r).qk = 0;
                    //rt
                    rTemp[co.rt].qi = b;
                    rTemp[co.rt].busy = true;
                    if (co.op == Operation.LW) {
                        rs.get(r).A = co.immediate;
                    }
                }
                if (co.op == Operation.SW || co.op == Operation.LW) {
                    rs.get(r).A = co.immediate;
                }
                break;
            }
        }
        if (removeuDaFila) {
            filaDeInstrucoesTemp.remove(0);
        }
        filaDeInstrucoes = new ArrayList<>(filaDeInstrucoesTemp);
        
    }

    //TO DO
    //TO DO: AJEITAR ISSUE (vk: valor, vj: end, A; imm)
    //INCOMPLETO
    public void execute() {
        //add
        if (ulaAdd.busy)  {
            ulaAdd.contClocks++;
            if (ulaAdd.contClocks >= ulaAdd.timeToFinish) {
                ulaAdd.doFPOperation();
                ulaAdd.done = true;           
            }
        } else {
            if(!ulaAdd.done){                
                //procurar algm pra executar
                for (ReservationStation re : reservationStationsSoma ){
                    if (re.qj == -1 && re.qk == -1){
                        ulaAdd.vj = re.vj;
                        ulaAdd.vk = re.vk;
                        ulaAdd.op = re.op;
                        ulaAdd.stationIndex = reservationStationsSoma.indexOf(re);
                        ulaAdd.busy = true;
                        ulaAdd.contClocks++;
                        if (ulaAdd.contClocks >= ulaAdd.timeToFinish) {
                            ulaAdd.doFPOperation();
                            ulaAdd.done = true;           
                        }
                        break;
                    }
                }
            }
        }
        if (ulaMem.busy) {
            //TO DO
            ulaAdd.contClocks++;
            //caso load
            
            //caso store
        } else {
            if(!ulaMem.done){
                //procurar algm pra executar
                for (int i = 0;i<N_Reservation_Mem;i++){
                    //caso store
                    if (reservationStationsMemoria.get(i).op == Operation.SW && i==0
                                && reservationStationsMemoria.get(i).qj == -1){                        
                        ulaMem.vj = reservationStationsMemoria.get(i).vj;
                        ulaMem.A = reservationStationsMemoria.get(i).A;
                        ulaMem.op = reservationStationsMemoria.get(i).op;
                        ulaMem.stationIndex = i;
                        ulaMem.busy = true;
                        ulaMem.contClocks++;                        
                        break;
                    }
                    //caso load,duas etapas ao mesmo tempo                    
                    //procurar store anterior
                    boolean storeAnterior = false;
                    for (int j = 0;j<i;j++)
                        if (reservationStationsMemoria.get(j).op == Operation.SW)
                            storeAnterior = true;
                    //"todos os store em rob anterior tem end diferente"
                    boolean endDiferente = false;
                    //INCOMPLETE !!!!!!!!!!!!!!!!!!!!!!!
                  //  for (int j = 0; j < reservationStationsMemoria.get(i).dest; j++ )
                     //   if(rob.get(j)  )
                    
                }
            }
        }
        if (ulaMult.busy)  {
            ulaMult.contClocks++;
            if (ulaMult.contClocks >= ulaAdd.timeToFinish) {
                ulaMult.doFPOperation();
                ulaMult.done = true;           
            }
        } else {
            if(!ulaMult.done){                
                //procurar algm pra executar
                for (ReservationStation re : reservationStationsMultiplicacao){
                    if (re.qj == -1 && re.qk == -1){
                        ulaMult.vj = re.vj;
                        ulaMult.vk = re.vk;
                        ulaMult.op = re.op;
                        ulaMult.stationIndex = reservationStationsMultiplicacao.indexOf(re);
                        ulaMult.busy = true;
                        ulaMult.contClocks++;
                        if (ulaMult.contClocks >= ulaMult.timeToFinish) {
                            ulaMult.doFPOperation();
                            ulaMult.done = true;           
                        }
                        break;
                    }
                }
            }
        }
    }
    //processador principal  
   public void write(){        
        //procurar algm pronto
        ULA ulas[] = new ULA[3];
        ULA utemp;
        ULA ula = null;
        ulas[0] = ulaMult; ulas[1] = ulaMem; ulas[2] = ulaAdd;
        boolean achou = false;
        for (int i =0;i<3;i++){
            if(ulas[i].done){
                utemp = ulas[i];
                //caso especial do store
                if(utemp.op == Operation.SW){
                    achou = (reservationStationsMemoria.get(utemp.stationIndex).qk == -1);
                    if (achou) {
                        ula = utemp;
                        break;
                    }
                }
                else{
                    achou = true;
                    ula = utemp;
                    break;
                }
            }
        }
        if(achou){
            ula.done = false;
            ula.busy = false;
            int sIndex = ula.stationIndex;
            int b = reservationStationsMemoria.get(sIndex).dest;
            if (ula.op == Operation.SW){
                 //index no rob
                robTemp.get(b).value = reservationStationsMemoria.get(sIndex).vk;
            }
            else{
                //caso normal, percorrer todos as estacoes
                for (ReservationStation re : reservationStationsSoma){
                    if(re.qj == b){
                        
                    }
                }
                
            }
            //TO DO: RETIRAR DA ESTACAO DE RESERVA
        }
        
       
   }
    public void process() {
        //fazer tudo dentro de um loop até acabar!!!!
        issue();
        execute(); //TO DO
        //    write(); //TO DO
        //    commit(); //TO DO

        //OBS: deve ser necessario copiar objeto a objeto, nao a lista  
        ArrayList<ReorderBuffer> rob2 = new ArrayList<>();
        for (ReorderBuffer r : robTemp) {
            rob2.add(new ReorderBuffer(r));
        }
        rob = new ArrayList<>(rob2);
        ArrayList<ReservationStation> r2 = new ArrayList<>();
        for (ReservationStation r : reservationStationsMemoriaTemp) {
            r2.add(new ReservationStation(r));
        }
        reservationStationsMemoria = new ArrayList<>(r2);
        r2.clear();
        for (ReservationStation r : reservationStationsMultiplicacaoTemp) {
            r2.add(new ReservationStation(r));
        }
        reservationStationsMultiplicacao = new ArrayList<>(r2);
        r2.clear();
        for (ReservationStation r : reservationStationsSomaTemp) {
            r2.add(new ReservationStation(r));
        }
        reservationStationsSoma = new ArrayList<>(r2);
        r2.clear();
        Register[] r3 = new Register[N_Register];
        for (int i = 0; i < N_Register; i++) {
            regs[i] = new Register(rTemp[i]);
        }
        memoriaVariaveis = Arrays.copyOf(memoriaVariaveisTemp, memoriaVariaveisTemp.length);

        //ATUALIZAR PC
        pc = pc + 4;
        clock++;
    }

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
        return Arrays.asList(regs);
    }
}
