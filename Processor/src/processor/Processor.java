package processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;



    //TO DO: AJEITAR ISSUE (vk: valor, vj: end, A; imm)

public class Processor {

    private final int N_Register = 32;
    private final int N_Reservation_Soma = 3;
    private final int N_Reservation_Mult = 2;
    private final int N_Reservation_Mem = 5;
    private final int N_ReorderBuffer = 10;

    public int pc = 0;
    private int clock = 0;
    private int instructionCounter = 0;
    private int prediction = 0;
    private int predictionBalance = 0;
    private int robId = 0;
    
    public int N_Instructions = 0;

    //OBS: cuidado se remover algm do rob, vai ter que ajustar indices nos em r[] e nas estacoes
    private final int[] Mem = new int[4096];
    private final Register[] Reg = new Register[N_Register];
    private final Vector<Command> commands = new Vector();
    private final ULA ulaAdd = new ULA(1);
    private final ULA ulaMult = new ULA(3);
    private final ULA ulaMem = new ULA(4);
    private final ArrayList<ReorderBuffer> rob = new ArrayList<>();
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
    
    public boolean nextClock() {
        boolean ret = false;
        ret = issue() && ret;
        ret = execute() && ret;
        ret = write() && ret;
        ret = commit() && ret;
        clock++;
        return ret;
    }

    private void readFile() {
        Command.setMap();
        String filename = "program1.txt";
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                commands.add(Command.parseCommand(line.split(";")[0], line.split(";")[1]));
                N_Instructions++;
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
        for (int i = 0; i < N_ReorderBuffer; i++) {
            rob.add(new ReorderBuffer(i + 1));
        }
        for (int i = 0; i < N_Register; i++) {
            Register temp = new Register(i);
            Reg[i] = temp;
        }

    }

    public boolean issue() {
        
        if (pc / 4 >= commands.size()) {
            return false;
        }
        Command co = commands.get(pc / 4);
        
        if (co.isJ()){
            pc = co.targetAddress;
            log(co, "Jumped to pc = " + pc);
            return true;
        }

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
        ReservationStation re = null;
        for (int r = 0; r < rs.size(); r++) {
            if (!rs.get(r).isBusy(clock)) {
                availResStat = true;
                if(rs.get(r).inserirComando(co, clock)){
                    hasIssued = true;
                    filaRob.add(rs.get(r).reorder);
                    re = rs.get(r);
                    log(co, co.op.toString() + " issued to Rob " + rs.get(r).reorder.id + " and ResStat " + re.id);
                }
                else{
                    log(co, "No Rob available, issue stalled");
                }
                break;
            }
        }
       
        if (hasIssued) {
            if (co.isR()) pc = pc + 4;
            else if (co.isJ()) pc = co.immediate;
            else{
                if (co.op != Operation.BEQ && co.op != Operation.BLE && co.op != Operation.BNE){
                    pc = pc + 4;
                }
                else if (prediction == 1){
                    pc = re.A;
                }
                else{
                    pc = pc + 4;
                }
            }
        }

        if (!availResStat) {
            log(co, "No ResStat available, issue stalled");
        }
        
        return true;
    }

    public boolean executeUlaAddMul(ULA ula){
        if (ula.toWrite || ula.isBusy(clock)) {
            return true;
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
            return false;     //nenhuma ResStat pra operar
        }
        ula.vj = chosen.vj;
        ula.vk = chosen.vk;
        ula.op = chosen.op;
        ula.A = chosen.A;
        chosen.reorder.state = State.EXECUTE;
        ula.station = chosen;
        ula.nonBusyClock = clock + ula.timeToFinish;
        ula.doFPOperation(clock);
        ula.toWrite = true;
        log(chosen.reorder.co, "started operation, to be finished in clock " + (ula.nonBusyClock - 1) + " taking " + ula.timeToFinish + " clocks");
        
        return true;
    }
    
    public boolean executeUlaMem(ULA ula){
        if (ula.toWrite || ula.isBusy(clock)) {
            return true;
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
                    || re.etapaLoad == 1 || (re.etapaLoad == 2 && !hasRobWithAddress(re.reorder)))) {
                chosen = re;
            }
        }
        if (chosen == null){
            return false;     //nenhuma ResStat pra operar
        }
        
        if (chosen.op == Operation.LW && chosen.etapaLoad == 1) {
            chosen.A = chosen.vj + chosen.A;
            chosen.reorder.state = State.EXECUTE;
            chosen.etapaLoad++;
            ula.nonBusyClock = clock + 1;
        }
        else if (chosen.op == Operation.LW && chosen.etapaLoad == 2) {
            //Le Mem[chosen.A]
            chosen.reorder.state = State.EXECUTE;
            ula.toWrite = true;
            ula.result = Mem[chosen.A];
            ula.op = Operation.LW;
            ula.station = chosen;
            ula.nonBusyClock = clock + ula.timeToFinish - 1;
        }
        else if (chosen.op == Operation.SW) {
            chosen.reorder.address = chosen.vj + chosen.A;
            //Escreve Mem[chosen.A]
            chosen.reorder.state = State.EXECUTE;
            chosen.reorder.readyClock = clock + 1;
            chosen.reorder.value = chosen.vk;
            chosen.nonBusyClock = clock + 1;
            ula.nonBusyClock = clock + ula.timeToFinish;
        }
        log(chosen.reorder.co, "started " + chosen.op.toString() + " operation, to be finished in clock " + (ula.nonBusyClock - 1));
        
        return true;
    }
    
    public boolean execute() {
        boolean ret = false;
        ret = executeUlaAddMul(ulaAdd) && ret;
        ret = executeUlaAddMul(ulaMult) && ret;
        ret = executeUlaMem(ulaMem) && ret;
        return ret;
    }

    //processador principal  
    public boolean write() {
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
            return false;
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
            b.address = ula.A;
        }
        else {
            b.value = station.vk;
            b.address = ula.A;
        }
        
        b.readyClock = clock + 1;
        b.state = State.WRITE;
        log(ula.station.reorder.co, "written in Rob " + b.id + ", Rob ready");

        ula.station.clear();
        ula.station.nonBusyClock = clock + 1;
        ula.clear();
        ula.nonBusyClock = clock + 1;
        
        return true;
    }

    public void clearMistake() {
        filaRob.clear();
        ulaAdd.clear();
        ulaMem.clear();
        ulaMult.clear();
        for (int i = 0; i < N_Register; i++) {
            Reg[i].clear();
        }
        for (int i = 0; i < N_ReorderBuffer; i++) {
            rob.get(i).clear();
        }
        for (int i = 0; i < N_Reservation_Mem; i++) {
            reservationStationsMemoria.get(i).clear();
        }
        for (int i = 0; i < N_Reservation_Mult; i++) {
            reservationStationsMultiplicacao.get(i).clear();
        }
        for (int i = 0; i < N_Reservation_Soma; i++) {
            reservationStationsSoma.get(i).clear();
        }
    }

    public boolean commit() {
        if (filaRob.isEmpty()){
            return false; //No Rob used
        }
        ReorderBuffer h = filaRob.peek();
        if (!h.isReady(clock)){
            return true; //Not ready
        }
        filaRob.poll();
        instructionCounter++;
        Register d = h.destination;
        
        //Libera o Rob
        h.state = State.COMMIT;
        h.nonBusyClock = clock + 1;
        log(h.co, "commited, " + (N_ReorderBuffer - filaRob.size()) + " free Robs");
        if (N_ReorderBuffer == filaRob.size()){
            System.out.println("Error:");
            for(int i=0; i<N_ReorderBuffer; i++){
                System.out.println(i + "=i: " + filaRob.peek().co.instruction);
                filaRob.add(filaRob.poll());
            }
        }
        
        //Altera o PC se for branch
        //BEQ, BLQ, BNE
        if (h.co.op == Operation.BEQ || h.co.op == Operation.BLE || h.co.op == Operation.BNE){
            if (prediction !=  h.value){    //mispredicted
                if (h.value == 1){
                    pc = h.address;
                }
                else{
                    pc = h.co.pc + 4;
                }
                log(h.co, "prediction failed, Robs cleared, pc now at " + pc + "(" + commands.get(pc/4).instruction + ")");
                clearMistake();
            }
            else {
                log(h.co, "prediction succedded");
            }
        }
        
        //Seta a memoria se for store
        //SW
        else if (h.co.op == Operation.SW) {
            log(h.co, "written " + h.value + " in Mem[" + h.address + "]");
            Mem[h.address] = h.value;
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
        
        return true;
    }
    
    private boolean hasRobWithAddress(ReorderBuffer cur) {
        Queue<ReorderBuffer> fila = new LinkedList<>(filaRob);
        while(fila.peek() != cur){
            if (fila.peek().address == cur.address) return true;
            fila.poll();
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
        if (filaRob.size() >= N_ReorderBuffer){
            return null;
        }
        ReorderBuffer r = rob.get(robId);
        robId++;
        if (robId >= N_ReorderBuffer) {
            robId = 0;
        }
        return r;
    }
}
