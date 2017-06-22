package processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Arrays;
import java.util.List;

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
            reservationStationsSoma.add(new ReservationStation(this));
            reservationStationsSomaTemp.add(new ReservationStation(this));
        }
        for (int i = 0; i < N_Reservation_Mult; i++) {
            reservationStationsMultiplicacao.add(new ReservationStation(this));
            reservationStationsMultiplicacaoTemp.add(new ReservationStation(this));
        }
        for (int i = 0; i < N_Reservation_Mem; i++) {
            reservationStationsMemoria.add(new ReservationStation(this));
            reservationStationsMemoriaTemp.add(new ReservationStation(this));
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
            filaDeInstrucoes.add(commands.get(pc / 4)); //tem que ser pc/4
            instructionCounter++;
        }
        //TO DO : verificar se é um jump condicional e fazer a predicao

        //da fila pra estacao de reserva e o rob
        if (filaDeInstrucoes.isEmpty()) {
            return;
        }
        Command co = filaDeInstrucoes.get(0);
        
        ArrayList<ReservationStation> rs;           //encontrar a estacao de reserva correspondente
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
            if (!rs.get(r).busy) {
                rs.get(r).inserirComando(co);
                filaDeInstrucoes.remove(0);
                break;
            }
        }
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
                        ulaAdd.station = re;
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
                        ulaMem.station = reservationStationsMemoria.get(i);
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
                        ulaMult.station = re;
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
        ArrayList<ULA> ulas = new ArrayList<>();
        ULA ula = null;
        ulas.add(ulaMult);
        ulas.add(ulaMem);
        ulas.add(ulaAdd);
        boolean achou = false;
        for (ULA utemp : ulas){
            if(utemp.done){
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
                ArrayList<ArrayList<ReservationStation>> allStations = new ArrayList<>();
                allStations.add(reservationStationsSoma);
                allStations.add(reservationStationsMemoria);
                allStations.add(reservationStationsMultiplicacao);
                for (ArrayList<ReservationStation> stations : allStations){
                    for (ReservationStation rs :stations){
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
                
            }
           
            ula.station.clear();
            
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

    public List<Register> getRegisters() {
        return Arrays.asList(regs);
    }

    public List<Register> getRegTemp() {
        return Arrays.asList(rTemp);
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
