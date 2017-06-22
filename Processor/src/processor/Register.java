/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processor;

/**
 *
 * @author Lucas Franca
 */
class Register {

    public int value = 0;
    public boolean status;
    public int qi = -1;
    public boolean busy = false;

    public Register(Register r) {
        this.value = r.value;
        this.status = r.status;
        this.qi = r.qi;
        this.busy = r.busy;
    }

    public Register() {
        this.value = 0;
        this.qi = -1;
        this.busy = false;
    }
}