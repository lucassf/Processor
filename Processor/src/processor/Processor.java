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
    private ULA ulaAddTemp = new ULA(1);
    private ULA ulaMultTemp = new ULA(3);
    private ULA ulaMemTemp = new ULA(4);
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

    //variaveis temporarias, necessarias para simular paralelismo
    private int[] memoriaVariaveisTemp = new int[1000];
    private Register[] registerStateTemp = new Register[N_Register];
    private ArrayList<ReorderBuffer> robTemp = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsSomaTemp = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsMultiplicacaoTemp = new ArrayList<>();
    private ArrayList<ReservationStation> reservationStationsMemoriaTemp = new ArrayList<>();

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
            reservationStationsSoma.add(new ReservationStation(this,"Add"));
            reservationStationsSomaTemp.add(new ReservationStation(this,"Add"));
        }
        for (int i = 0; i < N_Reservation_Mult; i++) {
            reservationStationsMultiplicacao.add(new ReservationStation(this,"Mult"));
            reservationStationsMultiplicacaoTemp.add(new ReservationStation(this,"Mult"));
        }
        for (int i = 0; i < N_Reservation_Mem; i++) {
            reservationStationsMemoria.add(new ReservationStation(this,"Load/Store"));
            reservationStationsMemoriaTemp.add(new ReservationStation(this,"Load/Store"));
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

    public void issue() {
        
        //TO DO : verificar se é um jump condicional e fazer a predicao

        if (filaDeInstrucoes.isEmpty()) {
            if (pc/4<commands.size())filaDeInstrucoes.add(commands.get(pc / 4));
            return;
        }
        Command co = filaDeInstrucoes.get(0);

        ArrayList<ReservationStation> rs;           //encontrar a estacao de reserva correspondente
        if (co.isEstacaoMem()) {
            rs = reservationStationsMemoriaTemp;
        } else if (co.isEstacaoMult()) {
            rs = reservationStationsMultiplicacaoTemp;
        } else {
            rs = reservationStationsSomaTemp;
        }
        for (int r = 0; r < rs.size(); r++) {
            if (!rs.get(r).busy) {
                rs.get(r).inserirComando(co);
                filaDeInstrucoes.remove(0);
                System.out.println("issue complete");
                pc = pc + 4;
                instructionCounter++;
                if (pc/4<commands.size()) {
                    filaDeInstrucoes.add(commands.get(pc / 4));
                }
                break;
            }
        }
        //colocar da memoria na fila
        
    }

    //TO DO
    public void executeUlaAddMul(ULA ula){
        if (ula.done) return;
        if (ula.busy) {
            ula.contClocks++;
            if (ula.contClocks >= ula.timeToFinish) {
                System.out.println("operation complete");
                ula.doFPOperation(clock);
                ula.done = true;           
            }
        }
        else {
            //procurar algm pra executar
            ArrayList<ReservationStation> rList = null;
            if (ula == ulaAddTemp) rList = reservationStationsSoma;
            if (ula == ulaMultTemp) rList = reservationStationsMultiplicacao;
            for (int i=0; i<rList.size(); i++) {
                ReservationStation re = rList.get(i);
                if (re.qj == -1 && re.qk == -1 && re.op != Operation.EMPTY) {
                    ula.vj = re.vj;
                    ula.vk = re.vk;
                    ula.op = re.op;
                    robTemp.get(re.dest).state = State.EXECUTE;
                    ula.stationId = i;
                    if (ula == ulaAddTemp) ula.stationType = StationType.ADD;
                    if (ula == ulaMultTemp) ula.stationType = StationType.MULT;
                    ula.busy = true;
                    ula.contClocks++;
                    if (ula == ulaAddTemp) System.out.println("operating add");
                    if (ula == ulaMultTemp) System.out.println("operating mult");
                    if (ula.contClocks >= ula.timeToFinish) {
                        System.out.println("operation complete");
                        ula.doFPOperation(clock);
                        ula.done = true;
                    }
                    break;
                }
            }
        }
    }
    //INCOMPLETO
    public void executeUlaMem(ULA ula){
        if (ulaMem.busy) {
            //TO DO
            //caso load

            //caso store
        } else {
            if (!ulaMem.done) {
                //procurar algm pra executar
                for (int i = 0; i < N_Reservation_Mem; i++) {
                    //caso store
                    if (reservationStationsMemoria.get(i).op == Operation.SW && i == 0
                            && reservationStationsMemoria.get(i).qj == -1) {
                        ulaMem.vj = reservationStationsMemoria.get(i).vj;
                        ulaMem.A = reservationStationsMemoria.get(i).A;
                        ulaMem.op = reservationStationsMemoria.get(i).op;
                        ulaMem.stationId = i;
                        ulaMem.stationType = StationType.MEM;
                        ulaMem.busy = true;
                        ulaMem.contClocks++;
                        break;
                    }
                    //caso load,duas etapas ao mesmo tempo                    
                    //procurar store anterior
                    boolean storeAnterior = false;
                    for (int j = 0; j < i; j++) {
                        if (reservationStationsMemoria.get(j).op == Operation.SW) {
                            storeAnterior = true;
                        }
                    }
                    //"todos os store em rob anterior tem end diferente"
                    boolean endDiferente = false;
                    //INCOMPLETE !!!!!!!!!!!!!!!!!!!!!!!
                    //  for (int j = 0; j < reservationStationsMemoria.get(i).dest; j++ )
                    //   if(rob.get(j)  )

                }
            }
        }
    }
    public void execute() {
        executeUlaAddMul(ulaAddTemp);
        executeUlaAddMul(ulaMultTemp);
        executeUlaMem(ulaMemTemp); //INCOMPLETO
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
            if (utemp.done && (ula == null || utemp.clock < ula.clock)) {
                //caso especial do store
                if(utemp.op == Operation.SW){
                    if(getStation(utemp).qk == -1) {
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
        //modificacoes devem ser feitas nas ula temp
        if (ula == ulaAdd) ula = ulaAddTemp;
        if (ula == ulaMult) ula = ulaMultTemp;
        if (ula == ulaMem) ula = ulaMemTemp;
        
        ReservationStation station = getStation(ula);
        int b = station.dest;
        if (ula.op != Operation.SW) {
            ArrayList<ReservationStation> allStations = new ArrayList<>();
            allStations.addAll(reservationStationsSomaTemp);
            allStations.addAll(reservationStationsMemoriaTemp);
            allStations.addAll(reservationStationsMultiplicacaoTemp);
            for (ReservationStation rs : allStations) {
                if (rs.qj == b) {
                    rs.vj = ula.result;
                    rs.qj = -1;
                }
                if (rs.qk == b) {
                    rs.vk = ula.result;
                    rs.qk = -1;
                }
            }
        }
        robTemp.get(b).value = ula.result;
        robTemp.get(b).ready = true;
        robTemp.get(b).state = State.WRITE;

        getStationTemp(ula).clear();
        ula.clear();
    }
    
    private void copyRsAndRob() {
        rob.clear();
        for (ReorderBuffer r : robTemp) {
            rob.add(new ReorderBuffer(r));
        }
        
        reservationStationsMemoria.clear();
        for (ReservationStation r : reservationStationsMemoriaTemp) {
            reservationStationsMemoria.add(new ReservationStation(r));
        }
        
        reservationStationsMultiplicacao.clear();
        for (ReservationStation r : reservationStationsMultiplicacaoTemp) {
            reservationStationsMultiplicacao.add(new ReservationStation(r));
        }
        
        reservationStationsSoma.clear();
        for (ReservationStation r : reservationStationsSomaTemp) {
            reservationStationsSoma.add(new ReservationStation(r));
        }
        
        for (int i = 0; i < N_Register; i++) {
            registerStat[i] = new Register(registerStateTemp[i]);
        }
        memoriaVariaveis = Arrays.copyOf(memoriaVariaveisTemp, memoriaVariaveisTemp.length);
    
        ulaAdd = new ULA(ulaAddTemp);
        ulaMem = new ULA(ulaMemTemp);
        ulaMult = new ULA(ulaMultTemp);
    }

    public void process() {
        //fazer tudo dentro de um loop até acabar!!!!
        issue();
        execute();
        write();
        //    commit(); //TO DO

        //OBS: deve ser necessario copiar objeto a objeto, nao a lista  
        copyRsAndRob();

        
        clock++;
    }
    
    public ReservationStation getStation(ULA ula) {
        return getResStat(ula.stationId, ula.stationType);
    }
    
    public ReservationStation getStationTemp(ULA ula) {
        return getResStatTemp(ula.stationId, ula.stationType);
    }
    
    public ReservationStation getResStat(int i, StationType t) {
        if (t == StationType.ADD) return reservationStationsSoma.get(i);
        if (t == StationType.MULT) return reservationStationsMultiplicacao.get(i);
        if (t == StationType.MEM) return reservationStationsMemoria.get(i);
        return null;
    }
    
    public ReservationStation getResStatTemp(int i, StationType t) {
        if (t == StationType.ADD) return reservationStationsSomaTemp.get(i);
        if (t == StationType.MULT) return reservationStationsMultiplicacaoTemp.get(i);
        if (t == StationType.MEM) return reservationStationsMemoriaTemp.get(i);
        return null;
    }

    public ArrayList<ReorderBuffer> getRob() {
        return rob;
    }

    public ArrayList<ReorderBuffer> getRobTemp() {
        return robTemp;
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

    public List<Register> getRegTemp() {
        return Arrays.asList(registerStateTemp);
    }

    public int getFirstNonBusyRob() {
        for (int j = 0; j < rob.size(); j++) {
            if (!rob.get(j).busy) {
                return j;
            }
        }
        return -1;
    }
}
