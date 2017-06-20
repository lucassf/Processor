package processor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

class RegisterStatus{
    public int qi;
    public boolean status;
}

class ReservationStation{
    public String name;
    public boolean busy;
    public Operation op;
    public int vj;
    public int vk;
    public int qj;
    public int qk;
    public int dest;
    public String A;
}

class ReorderBuffer{
    public boolean busy;
    public Operation instruction;
    public State state;
    public int destination;
    public String value;
}

public class Processor {
    
    private RegisterStatus registerStatus[] = new RegisterStatus[32];
    private Vector<Command> commands;
    private Vector<ReorderBuffer> ro;
    private ReservationStation reservationStations[] = new ReservationStation[12];
    
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
    }
    
    public void nextClock(){
        
    }
    
    
}
