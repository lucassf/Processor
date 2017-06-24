package processor;

class Register {

    public int value;
    public ReorderBuffer qi;
    public boolean busy = false;
    public int id;

    public Register(int id) {
        this.id = id;
        this.value = 0;
    }
    
    public void clear() {
        this.qi = null;
        this.busy = false;
    }
}