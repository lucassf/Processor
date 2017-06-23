package processor;

class ReorderBuffer {

    public boolean busy;
    public String instruction;
    public State state;
    public boolean ready;
    public int destination;
    public String destinationType; //se é Registrador, memoria...
    public int value;
    public int address; //usado nos branches e store

    public ReorderBuffer(ReorderBuffer r) {
        this.busy = r.busy;
        this.instruction = r.instruction;
        this.state = r.state;
        this.ready = r.ready;
        this.destination = r.destination;
        this.destinationType = r.destinationType;
        this.value = r.value;
        this.address = r.address;
    }

    public ReorderBuffer() {
        this.busy = false;
        instruction = "";
        state = State.ISSUE;
        this.ready = false;
        destination = -1;
        destinationType = "";
        value = -1;
    }
}