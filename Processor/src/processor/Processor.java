package processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class RegisterStatus{
    public int qi = -1;
    public boolean status = false;
}

class ReservationStation{
    public String name = "";
    public boolean busy = false;
    public Operation op = Operation.EMPTY;
    public int vj = -1;
    public int vk = -1;
    public int qj = -1;
    public int qk = -1;
    public int dest = -1;
    public String A = "";

    public ReservationStation(String name) {
        this.name = name;
    }
}

class ReorderBuffer{
    public boolean busy = false;
    public Operation instruction = Operation.EMPTY;
    public State state = State.ISSUER;
    public int destination = -1;
    public String value = "";
}

public class Processor {
    private final int  N_RegisterStatus = 32;
    private int pc = 0;
    private int clock = 0;
    
    private List<RegisterStatus> registerStatus = Arrays.asList(new RegisterStatus[32]);
    private List<Command> commands = new ArrayList<>();
    private List<ReorderBuffer> ro = new ArrayList<>();
    private List<Command> filaDeInstrucoes = new ArrayList<>();
    private List<ReservationStation> reservationStationsSoma = Arrays.asList(new ReservationStation[3]);
    private List<ReservationStation> reservationStationsMultiplicacao = Arrays.asList(new ReservationStation[2]);
    private List<ReservationStation> reservationStationsMemoria = Arrays.asList(new ReservationStation[5]);
    
    //variaveis temporarias, necessarias para simular paralelismo
    private final List<ReorderBuffer> roTemp = new ArrayList<>();
    private final List<Command> filaDeInstrucoesTemp = new ArrayList<>();
    private final List<ReservationStation> reservationStationsSomaTemp = Arrays.asList(new ReservationStation[3]);
    private final List<ReservationStation> reservationStationsMultiplicacaoTemp = Arrays.asList(new ReservationStation[2]);
    private final List<ReservationStation> reservationStationsMemoriaTemp = Arrays.asList(new ReservationStation[5]);
    
    private void readFile(){
        Command.setMap();
        String filename = "program.txt";
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                commands.add(Command.parseCommand(line.split(" ")[0]));
            }
        }catch(IOException e){
            System.out.println("Erro na leitura");
        }
    }    
    
    public Processor(){
        readFile();
        initLists();
    }
    public void issue(){
        //colocar da memoria na fila
        filaDeInstrucoesTemp.add(getCommands().get(getPc()/4)); //tem que ser pc/4
        if(!filaDeInstrucoes.isEmpty()){
            Command co = getFilaDeInstrucoes().get(0);
            //ver se a estacao de reserv correspondente e adicionar se possivel
            if (co.isEstacaoMem()){
                
            }
            if(co.isEstacaoMult()){
                int i;
                
            }
            if(co.isEstacaoSoma()){
                for(int i = 0;i<3;i++);
            }
        }
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
    public int FindCorrespondingSource (int Source_Register, int Instruction_index)
    {
        for (int i = Instruction_index - 1; i >= 0; i++)
            if (getCommands().get(i).rd == Source_Register)
                return getCommands().get(i).T_Dest;
        return Source_Register;    
    }
    
    public void nextClock(){
        
    }

    public int getPc() {
        return pc;
    }

    public int getClock() {
        return clock;
    }

    public List<Command> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    public List<ReorderBuffer> getRo() {
        return Collections.unmodifiableList(ro);
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
        return Collections.unmodifiableList(registerStatus);
    }

    private void initLists() {
        for (int i=0;i<registerStatus.size();i++){
            registerStatus.set(i, new RegisterStatus());
        }for (int i=0;i<reservationStationsMemoria.size();i++){
            reservationStationsMemoria.set(i, new ReservationStation("Load/Store"));
        }for (int i=0;i<reservationStationsSoma.size();i++){
            reservationStationsSoma.set(i, new ReservationStation("Add"));
        }for (int i=0;i<reservationStationsMultiplicacao.size();i++){
            reservationStationsMultiplicacao.set(i, new ReservationStation("Mult"));
        }
    }
    
    
}
