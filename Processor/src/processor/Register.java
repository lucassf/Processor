package processor;

class Register {

    public int value;
    public boolean status;
    public ReorderBuffer qi;
    public boolean busy = false;
    public int id;

    public Register(int id) {
        this.value = 0;
        this.qi = null;
        this.busy = false;
        this.status = false;
        this.id = id;
    }
}