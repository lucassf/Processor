package processor;

class Register {

    public int value;
    public boolean status;
    public int qi;
    public boolean busy = false;

    public Register(Register r) {
        this.value = r.value;
        this.status = r.status;
        this.qi = r.qi;
        this.busy = r.busy;
    }

    public Register() {
        this.value = -1;
        this.qi = -1;
        this.busy = false;
        this.status = false;
    }
}