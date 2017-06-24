package processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Arrays;
import java.util.List;



    //TO DO: AJEITAR ISSUE (vk: valor, vj: end, A; imm)

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
    private int[] memoriaVariaveis = new int[4096];
    private Register[] registerStat = new Register[N_Register];
    private Vector<Command> commands = new Vector();

    //OBS: cuidado se remover algm do rob, vai ter que ajustar indices nos em r[] e nas estacoes
    private ArrayList<ReorderBuffer> rob = new ArrayList<>();
    private ArrayList<Command> filaDeInstrucoes = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsSoma = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsMultiplicacao = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsMemoria = new ArrayList<>();

    public Processor() {
        initEstacoesReservaERobERegister();
        readFile();
    }

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
            reservationStationsSoma.add(new ReservationStation(this, "Add", i+1));
        }
        for (int i = 0; i < N_Reservation_Mult; i++) {
            reservationStationsMultiplicacao.add(new ReservationStation(this, "Mult", i+1));
        }
        for (int i = 0; i < N_Reservation_Mem; i++) {
            reservationStationsMemoria.add(new ReservationStation(this, "Load/Store", i+1));
        }
        for (int i = 0; i < 10; i++) {
            rob.add(new ReorderBuffer(i + 1));
        }
        for (int i = 0; i < N_Register; i++) {
            Register temp = new Register(i + 1);
            registerStat[i] = temp;
        }

    }

    public void issue() {
        
        //TO DO : verificar se é um jump condicional e fazer a predicao

        if (filaDeInstrucoes.isEmpty()) {
            if (pc / 4 < commands.size()) {
                filaDeInstrucoes.add(commands.get(pc / 4));
            }
            return;
        }
        Command co = filaDeInstrucoes.get(0);

        ArrayList<ReservationStation> rs;           //encontrar a estacao de reserva correspondente
        if (co.isEstacaoMem()) {
            rs = reservationStationsMemoria;
        } else if (co.isEstacaoMult()) {
            rs = reservationStationsMultiplicacao;
        } else {
            rs = reservationStationsSoma;
        }
        
        boolean hasIssued = false;
        for (int r = 0; r < rs.size(); r++) {
            if (!rs.get(r).isBusy(clock)) {
                if(rs.get(r).inserirComando(co, clock)){
                    hasIssued = true;
                }
                else System.out.println("Issue failed");
                filaDeInstrucoes.remove(0);
                pc = pc + 4;
                instructionCounter++;
                if (pc / 4 < commands.size()) {
                    filaDeInstrucoes.add(commands.get(pc / 4));
                }
                break;
            }
        }
        //colocar da memoria na fila
        
    }

    //TO DO
    public void executeUlaAddMul(ULA ula){
        if (ula.operating || ula.isBusy(clock)) {
            return;
        }
        //procurar algm pra executar
        ArrayList<ReservationStation> rList = null;
        if (ula == ulaAdd) {
            rList = reservationStationsSoma;
        }
        if (ula == ulaMult) {
            rList = reservationStationsMultiplicacao;
        }
        for (int i = 0; i < rList.size(); i++) {
            ReservationStation re = rList.get(i);
            if (re.qj == null && re.qk == null && re.op != Operation.EMPTY && clock > re.issuedClock) {
                ula.vj = re.vj;
                ula.vk = re.vk;
                ula.op = re.op;
                re.reorder.state = State.EXECUTE;
                ula.station = re;
                ula.nonBusyClock = clock + ula.timeToFinish;
                ula.operating = true;
                break;
            }
        }
    }
    //INCOMPLETO
    public void executeUlaMem(ULA ula){
    }
    public void execute() {
        executeUlaAddMul(ulaAdd);
        executeUlaAddMul(ulaMult);
        executeUlaMem(ulaMem); //INCOMPLETO
    }

    //processador principal  
    public void write() {
        //procurar algm pronto
        ArrayList<ULA> ulas = new ArrayList<>();
        ULA ula = null;
        ulas.add(ulaMult);
        ulas.add(ulaMem);
        ulas.add(ulaAdd);
        for (ULA utemp : ulas) {
            if (utemp.operating && !utemp.isBusy(clock) && (ula == null || utemp.nonBusyClock < ula.nonBusyClock)) {
                //caso especial do store
                if(utemp.op == Operation.SW){
                    if(utemp.station.qk == null) {
                        ula = utemp;
                    }
                }
                else {
                    ula = utemp;
                }
            }
        }
        if (ula == null) {
            return;
        }
        
        ReservationStation station = ula.station;
        ReorderBuffer b = station.reorder;
        if (ula.op != Operation.SW) {
            ArrayList<ReservationStation> allStations = new ArrayList<>();
            allStations.addAll(reservationStationsSoma);
            allStations.addAll(reservationStationsMemoria);
            allStations.addAll(reservationStationsMultiplicacao);
            for (ReservationStation rs : allStations) {
                if (rs.qj == b) {
                    rs.vj = ula.result;
                    rs.qj = null;
                }
                if (rs.qk == b) {
                    rs.vk = ula.result;
                    rs.qk = null;
                }
            }
            b.value = ula.result;
            b.ready = true;
            b.state = State.WRITE;
        }
        else {
            
        }

        ula.station.clear();
        ula.clear();
    }

    public void Commit() {

        /*ArrayList<ReservationStation> allStations = new ArrayList<>();
        allStations.addAll(reservationStationsSomaTemp);
        allStations.addAll(reservationStationsMemoriaTemp);
        allStations.addAll(reservationStationsMultiplicacaoTemp);
        for (ReservationStation rs : allStations) //Percorre todas as estações
        {
            if (rs.op != Operation.SW) //Se não for store
            {
                //Se estiver pronto, escreve no registrador de destino
                if (rs.ready) {
                    int b = rs.dest;
                    registerStat[b].value = rs.value;
                    rs.busy = false;
                    if (registerStat[b].reorder == b) {
                        registerStat[b].busy = false;
                    }
                }
            } else //Caso do Store
            {
                //Se estiver pronto, escreve no endereço de destino
                if (rs.ready) {
                    int TargetAddress = rs.A;
                    memoriaVariaveis[TargetAddress] = rs.value;
                    rs.busy = false;
                }
            }
        }*/
    }
    
    
    
    
    
    public void process() {
        //fazer tudo dentro de um loop até acabar!!!!
        issue();
        execute();
        write();

        
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
        return Arrays.asList(registerStat);
    }

    public ReorderBuffer getFirstNonBusyRob() {
        for (ReorderBuffer r : rob) {
            if (!r.isBusy(clock)) {
                return r;
            }
        }
        return null;
    }
}
