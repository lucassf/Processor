package processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

class RegisterStatus{
    public int qi = 0;
    public boolean status = false;
}

class ReservationStation{
    public boolean busy = false;
    public Operation op = Operation.EMPTY;
    public int vj = -1;
    public int vk = -1;
    public int qj = -1;
    public int qk = -1;
    public int dest = -1;
    public String A = "";
}

class ReorderBuffer{
    public boolean busy = false;
    public Operation instruction = Operation.EMPTY;
    public State state = State.ISSUER;
    public int destination = -1;
    public String value = "";
}

public class Processor {

    
    private final List<RegisterStatus> registerStatus = Arrays.asList(new RegisterStatus[32]);
    private final List<Command> commands = new ArrayList<>();
    private final List<ReorderBuffer> ro = new ArrayList<>();
    private final List<ReservationStation> reservationStations = Arrays.asList(new ReservationStation[12]);
    
    private void readFile(){
        
        Command.setMap();
        String filename = "program.txt";
        try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                getCommands().add(Command.parseCommand(line.split(" ")[0]));
            }
        }catch(IOException e){
            System.out.println("Erro na leitura");
        }
    }    
    
    public Processor(){
        readFile();
    }
    
    public void nextClock(){
        
    }

    public List<RegisterStatus> getRegisterStatus() {
        return Collections.unmodifiableList(registerStatus);
    }

    public List<Command> getCommands() {
        return Collections.unmodifiableList(commands);
    }

    public List<ReorderBuffer> getRo() {
        return Collections.unmodifiableList(ro);
    }

    public List<ReservationStation> getReservationStations() {
        return Collections.unmodifiableList(reservationStations);
    }
    
}
