package processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Arrays;
import java.util.List;

class Register {

    public int value;
    public boolean status;
    public int qi;
    public boolean busy;

    public Register(Register r) {
        this.value = r.value;
        this.status = r.status;
        this.qi = r.qi;
        this.busy = r.busy;
    }

    public Register() {
        this.value = -1;
        this.qi = -1;
        this.busy = this.status = false;
    }
}

class ReservationStation {

    public String instruction;
    public boolean busy;
    public Operation op;
    public int dest;
    public int vj;
    public int vk;
    public int qj;
    public int qk;
    public int A;

    public ReservationStation(ReservationStation r) {
        this.instruction = r.instruction;
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
        instruction = "";
        busy = false;
        op = Operation.EMPTY;
        dest = vj = vk = qj = qk = A = -1;
    }
}

class ULA {

    public boolean busy = false;
    public boolean done = false;
    public int result = 0;
    public int contClocks = 0;
    public int timeToFinish;
    public ReservationStation rStationOperando;

    public ULA(int i) {
        this.timeToFinish = i;
    }

    public int doFPOperation() {
        if (rStationOperando.op == Operation.ADD || rStationOperando.op == Operation.ADDI) {
            return rStationOperando.vj + rStationOperando.vk;
        }
        if (rStationOperando.op == Operation.MUL) {
            return rStationOperando.vj * rStationOperando.vk;
        }
        if (rStationOperando.op == Operation.SUB) {
            return rStationOperando.vj - rStationOperando.vk;
        }
        if (rStationOperando.op == Operation.BNE) {
            return rStationOperando.vj - rStationOperando.vk;
        }
        if (rStationOperando.op == Operation.SUB) {
            return rStationOperando.vj - rStationOperando.vk;
        }
        if (rStationOperando.op == Operation.SUB) {
            return rStationOperando.vj - rStationOperando.vk;
        }
        return 0; //para as instrucoes que nao tem retorno
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

    public ReorderBuffer(ReorderBuffer r) {
        this.busy = r.busy;
        this.instruction = r.instruction;
        this.state = r.state;
        this.ready = r.ready;
        this.destination = r.destination;
        this.destinationType = r.destinationType;
        this.value = r.value;
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
    private int[] memoriaVariaveis = new int[1000];
    private Register[] registerStat = new Register[N_Register];
    private Vector<Command> commands = new Vector();

    //OBS: cuidado se remover algm do rob, vai ter que ajustar indices nos em r[] e nas estacoes
    private ArrayList<ReorderBuffer> rob = new ArrayList<>();
    private ArrayList<Command> filaDeInstrucoes = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsSoma = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsMultiplicacao = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsMemoria = new ArrayList<>();

    //variaveis temporarias, necessarias para simular paralelismo
    private int[] memoriaVariaveisTemp = new int[1000];
    private Register[] registerStateTemp = new Register[N_Register];
    private ArrayList<ReorderBuffer> robTemp = new ArrayList<>();
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
            registerStat[i] = temp;
            registerStateTemp[i] = temp;
        }

    }

    public Processor() {
        initEstacoesReservaERobERegister();
        readFile();
    }

    public void issue() {
        
        //TO DO : verificar se é um jump condicional e fazer a predicao

        //da fila pra estacao de reserva e o rob
        if (!filaDeInstrucoes.isEmpty()) {
            Command co = filaDeInstrucoes.get(0);
            //encontrar a estacao de reserva correspondente
            ArrayList<ReservationStation> reservationStation;
            if (co.isEstacaoMem()) {
                reservationStation = reservationStationsMemoriaTemp;
            }
            else if (co.isEstacaoMult()) {
                reservationStation = reservationStationsMultiplicacaoTemp;
            }
            else {
                reservationStation = reservationStationsSomaTemp;
            }
            int r = 0, b =0;
            while (r<reservationStation.size()&&reservationStation.get(r).busy)r++;
            while (b<rob.size() && rob.get(b).busy)b++;
            if (r == reservationStation.size() || b==rob.size())return;
            
            ReservationStation RS = new ReservationStation();
            int rs = co.rs;
            int rd = co.rd;
            int rt = co.rt;
            int immediate = co.immediate;
            
            robTemp.get(b).ready = false;
            robTemp.get(b).busy = true;
            robTemp.get(b).instruction = co.name;
            robTemp.get(b).state = State.ISSUE;
            
            if (registerStat[rs].busy){
                int h = registerStat[rs].qi;
                if (rob.get(h).ready){
                    RS.vj = rob.get(h).value;
                    RS.qj = -1;
                }else{
                    RS.vj = -1;
                    RS.qj = rob.get(h).value;
                }
            }else{
                RS.vj = registerStat[rs].value;
                RS.qj = -1;
            }
            RS.busy = true;
            RS.dest = b;
            RS.instruction = co.name;
            
            if (co.isR()){
                if(registerStat[rt].busy){
                    int h = registerStat[rt].qi;
                    if (rob.get(h).ready){
                        reservationStation.get(r).vk = rob.get(h).value;
                        reservationStation.get(r).qk = -1;
                    }else{
                        reservationStation.get(r).vk = -1;
                        reservationStation.get(r).qk = rob.get(h).value;
                    }
                }else{
                    reservationStation.get(r).vk = registerStat[rt].value;
                    reservationStation.get(r).qk = -1;
                }
                robTemp.get(b).destination = rd;
            }else if (co.isI()){
                reservationStation.get(r).vk = immediate;
                reservationStation.get(r).qk = -1;
                robTemp.get(b).destination = rt;           
            }if (co.isI()){
                RS.A = immediate;
                registerStat[rt].qi = b;
                registerStat[rt].busy = true;
                robTemp.get(b).destination = rt;
            }
        }
        filaDeInstrucoes.remove(0);
        //colocar da memoria na fila
        if (pc / 4 < commands.size()) {
            filaDeInstrucoes.add(commands.get(pc / 4)); //tem que ser pc/4
            instructionCounter++;
        }
    }

    //TO DO
    public void execute() {
        //add
        if (ulaAdd.busy) {
            if (ulaAdd.contClocks == ulaAdd.timeToFinish) {
                ulaAdd.doFPOperation();
                //FALTA OS BRANCHES
            }
        } else {

        }
        if (ulaMem.busy) {

        } else {

        }
        if (ulaMult.busy) {

        } else {

        }
    }
    //processador principal

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
            registerStat[i] = new Register(registerStateTemp[i]);
        }
        memoriaVariaveis = Arrays.copyOf(memoriaVariaveisTemp, memoriaVariaveisTemp.length);

        //ATUALIZAR PC
        pc = pc + 4;
        clock++;
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
        return Arrays.asList(registerStat);
    }
}
