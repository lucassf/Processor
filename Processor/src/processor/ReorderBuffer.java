package processor;

class ReorderBuffer {

    public int nonBusyClock;
    public String instruction;
    public State state;
    public boolean ready;
    public int destination;
    public String destinationType; //se é Registrador, memoria...
    public int value;
    public int address; //usado nos branches e store
    public int id;

    public ReorderBuffer(int id) {
        nonBusyClock = -1;
        instruction = "";
        state = State.ISSUE;
        this.ready = false;
        destination = -1;
        destinationType = "";
        value = -1;
        this.id = id;
    }
    
    public boolean isBusy(int curClock){
        return curClock < nonBusyClock;
    }
}