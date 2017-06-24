package processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;



    //TO DO: AJEITAR ISSUE (vk: valor, vj: end, A; imm)

public class Processor {

    private final int N_Register = 32;
    private final int N_Reservation_Soma = 3;
    private final int N_Reservation_Mult = 2;
    private final int N_Reservation_Mem = 5;

    private int pc = 0;
    private int clock = 0;
    private int instructionCounter = 0;
    private int prediction = 0;
    private int predictionBalance = 0;

    //OBS: cuidado se remover algm do rob, vai ter que ajustar indices nos em r[] e nas estacoes
    private final int[] Mem = new int[4096];
    private final Register[] Reg = new Register[N_Register];
    private final Vector<Command> commands = new Vector();
    private final ULA ulaAdd = new ULA(1);
    private final ULA ulaMult = new ULA(3);
    private final ULA ulaMem = new ULA(4);
    private final ArrayList<ReorderBuffer> rob = new ArrayList<>();
    private final ArrayList<Command> filaDeInstrucoes = new ArrayList<>();
    private final ArrayList<ReservationStation> reservationStationsSoma = new ArrayList<>();
    private final ArrayList<ReservationStation> reservationStationsMultiplicacao = new ArrayList<>();
    private final ArrayList<ReservationStation> reservationStationsMemoria = new ArrayList<>();
    private final Queue<ReorderBuffer> filaRob = new ConcurrentLinkedQueue<>();
    
    public Processor() {
        initEstacoesReservaERobERegister();
        readFile();
    }

    public void log(Command co, String message) {
        System.out.println("Clock " + clock + ", Instruction " + co.instruction + ": " + message);
    }
    
    public void nextClock() {
        issue();
        execute();
        write();
        commit();
        clock++;
    }

    private void readFile() {
        Command.setMap();
        String filename = "program.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                commands.add(Command.parseCommand(line.split(";")[0], line.split(";")[1]));
            }
            for(int i = 0; i < commands.size(); i++){
                commands.get(i).pc = 4*i;
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
            Reg[i] = temp;
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
        boolean availResStat = false;
        for (int r = 0; r < rs.size(); r++) {
            if (!rs.get(r).isBusy(clock)) {
                availResStat = true;
                if(rs.get(r).inserirComando(co, clock)){
                    hasIssued = true;
                    filaRob.add(rs.get(r).reorder);
                    log(co, "issued to Rob " + rs.get(r).reorder.id);
                }
                else{
                    System.out.println("Issue failed");
                    log(co, "No Rob available, issue stalled");
                }
                break;
            }
        }
       
        if (hasIssued) {
            filaDeInstrucoes.remove(0);
            pc = pc + 4;
            if (pc / 4 < commands.size()) {
                filaDeInstrucoes.add(commands.get(pc / 4));
            }
        }

        if (!availResStat) {
            log(co, "No ResStat available, issue stalled");
        }
        
    }

    //TO DO
    public void executeUlaAddMul(ULA ula){
        if (ula.toWrite || ula.isBusy(clock)) {
            return;
        }
        
        //procurar alguém pra executar
        ArrayList<ReservationStation> rList = null;
        if (ula == ulaAdd) {
            rList = reservationStationsSoma;
        }
        if (ula == ulaMult) {
            rList = reservationStationsMultiplicacao;
        }
        ReservationStation chosen = null;
        for (int i = 0; i < rList.size(); i++) {
            ReservationStation re = rList.get(i);
            if (re.qj == null && re.qk == null &&
                    re.op != Operation.EMPTY && clock > re.issuedClock &&
                    (chosen == null || chosen.issuedClock > re.issuedClock)) {
                chosen = re;
            }
        }
        if (chosen == null){
            return;     //nenhuma ResStat pra operar
        }
        ula.vj = chosen.vj;
        ula.vk = chosen.vk;
        ula.op = chosen.op;
        chosen.reorder.state = State.EXECUTE;
        ula.station = chosen;
        ula.nonBusyClock = clock + ula.timeToFinish;
        ula.doFPOperation(clock);
        ula.toWrite = true;
        log(chosen.reorder.co, "started operation, to be finished in clock " + (ula.nonBusyClock - 1));
    }
    
    public void executeUlaMem(ULA ula){
        if (ula.toWrite || ula.isBusy(clock)) {
            return;
        }
        
        //procurar alguém pra executar
        ArrayList<ReservationStation> rList = reservationStationsMemoria;
        ReservationStation chosen = null;
        for (int i = 0; i < rList.size(); i++) {
            ReservationStation re = rList.get(i);
            if (re.qj == null &&
                    re.op != Operation.EMPTY && clock > re.issuedClock &&
                    (chosen == null || chosen.issuedClock > re.issuedClock) &&
                    ((re.op == Operation.SW && re.reorder == filaRob.peek())
                    || re.etapaLoad == 1 || (re.etapaLoad == 2 && hasRobWithAddress(re.A)))) {
                chosen = re;
            }
        }
        if (chosen == null){
            return;     //nenhuma ResStat pra operar
        }
        
        if (chosen.op == Operation.LW && chosen.etapaLoad == 1) {
            chosen.A = chosen.vj + chosen.A;
            chosen.reorder.state = State.EXECUTE;
            chosen.etapaLoad++;
            log(chosen.reorder.co, "finished execute step 1");
        }
        else if (chosen.op == Operation.LW && chosen.etapaLoad == 2) {
            //Le Mem[chosen.A]
            chosen.reorder.state = State.EXECUTE;
            ula.toWrite = true;
            ula.result = Mem[chosen.A];
            log(chosen.reorder.co, "finished execute step 2");
        }
        else if (chosen.op == Operation.SW) {
            chosen.reorder.address = chosen.vj + chosen.A;
            //Escreve Mem[chosen.A]
            chosen.reorder.state = State.EXECUTE;
            chosen.reorder.readyClock = clock + 1;
            log(chosen.reorder.co, "finished execute");
        }
        ula.nonBusyClock = clock + ula.timeToFinish;
        
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
            if (utemp.toWrite && !utemp.isBusy(clock) && (ula == null || utemp.nonBusyClock < ula.nonBusyClock)) {
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
                    log(ula.station.reorder.co, "Result " + ula.result + " written in  vj ResStat " + rs.reorder.co.instruction);
                }
                if (rs.qk == b) {
                    rs.vk = ula.result;
                    rs.qk = null;
                    log(ula.station.reorder.co, "Result " + ula.result + " written in  vk ResStat " + rs.reorder.co.instruction);
                }
            }
            b.value = ula.result;
        }
        else {
            b.value = station.vk;
        }
        
        b.readyClock = clock + 1;
        b.state = State.WRITE;
        log(ula.station.reorder.co, "written in Rob " + b.id + ", Rob ready");

        ula.station.clear();
        ula.station.nonBusyClock = clock + 1;
        ula.clear();
        ula.nonBusyClock = clock + 1;
    }

    public void commit() {
        if (filaRob.isEmpty()){
            return; //No Rob used
        }
        ReorderBuffer h = filaRob.peek();
        if (!h.isReady(clock)){
            return; //Not ready
        }
        filaRob.poll();
        instructionCounter++;
        Register d = h.destination;
        
        //Libera o Rob
        h.state = State.COMMIT;
        h.nonBusyClock = clock + 1;
        
        //Altera o PC se for branch
        //BEQ, BLQ, BNE
        if (h.co.op == Operation.BEQ || h.co.op == Operation.BLE || h.co.op == Operation.BNE){
            if (prediction !=  h.value){    //mispredicted
                while(!filaRob.isEmpty()) {
                    filaRob.peek().clear();
                    filaRob.poll();
                }
                log(h.co, "prediction failed, Robs cleared");
            }
            else {
                log(h.co, "prediction succedded");
            }
            if (h.value == 1){
                pc = h.address;
            }
        }
        
        //Seta a memoria se for store
        //SW
        else if (h.co.op == Operation.SW) {
            Mem[h.address] = h.value;
                log(h.co, "written " + h.value + " in Mem[" + h.address + "]");
        }
        
        //Qualquer outro comando, escreve no registrador
        else {
            if (h.destination != null){
                h.destination.value = h.value;
                if (h.destination.qi == h){
                    h.destination.busy = false;
                }
                log(h.co, "written " + h.value + " in Reg[" + h.destination.id + "]");
            }
        }
    }
    
    private boolean hasRobWithAddress(int address) {
        for(ReorderBuffer r : rob) {
            if (r.address == address && r.isBusy(clock)) return true;
        }
        return false;
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
        return Arrays.asList(Reg);
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
