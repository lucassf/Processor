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
class ReservationStation {

    public String name;
    public boolean busy;
    public Operation op;
    public int dest;
    public int vj;
    public int vk;
    public int qj;
    public int qk;
    public int A;

    public ReservationStation(ReservationStation r) {
        this.name = r.name;
        this.busy = r.busy;
        this.op = r.op;
        this.dest = r.dest;
        this.vj = r.vj;
        this.vk = r.vk;
        this.qj = r.qj;
        this.qk = r.qk;
        this.A = r.A;
    }

    public ReservationStation() {
        name = "";
        busy = false;
        op = Operation.EMPTY;
        dest = vj = vk = qj = qk = A = -1;
    }
}