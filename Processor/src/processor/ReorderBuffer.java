package processor;


class ReorderBuffer {

    public int nonBusyClock;
    public int readyClock;
    
    public State state;
    public Register destination = null;
    public ReservationStation station = null;
    public Command co;
    
    public int value;
    public int address; //usado nos branches e store
    public int id;

    public ReorderBuffer(int id) {
        this.id = id;
        clear();
    }
    
    public boolean isBusy(int curClock){
        return curClock < nonBusyClock;
    }
    
    public boolean isReady(int curClock){
        return curClock >= readyClock;
    }
    
    public void clear() {
        state = State.EMPTY;
        this.nonBusyClock = -1;
        this.readyClock = Integer.MAX_VALUE;
        this.co = null;
        destination = null;
        value = -1;
        destination = null;
        station = null;
    }
}
