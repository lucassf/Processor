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
class ULA {

    public boolean busy = false;
    public boolean done = false;
    public int result = -1;// branch: 0 se nao jump, 1 se jump
    public int contClocks = 0;
    public int timeToFinish;
    public int vj = -1;
    public int vk = -1; 
    public int A = -1;
    public int clock;
    public Operation op;
    public int stationId; //estacao de reserva em execucao
    public StationType stationType;
    
    public ULA(int i) {
        this.timeToFinish = i;
    }
    
    public ULA(ULA ula) {
        this.busy = ula.busy;
        this.done = ula.done;
        this.result = ula.result;
        this.contClocks = ula.contClocks;
        this.timeToFinish = ula.timeToFinish;
        this.vj = ula.vj;
        this.vk = ula.vk; 
        this.A = ula.A;
        this.clock = ula.clock;
        this.op = ula.op;
        this.stationId = ula.stationId;
    }
    
    public void clear() {
        result = -1;
        contClocks = 0;
        vj = -1;
        vk = -1; 
        A = -1;
        busy = false;
        done = false;
    }

    public void doFPOperation(int clock) {
        this.clock = clock;
        if (op == Operation.ADD || op == Operation.ADDI) {
            result = vj + vk;
        }
        if (op == Operation.MUL) {
            result = vj * vk;
        }
        if (op == Operation.SUB) {
            result = vj - vk;
        }
        if (op == Operation.BNE) {
            if (vk != vj)
                result = 1;
            else
                result = 0;
        }
        if (op == Operation.BLE ) {
            if (vj<=vk)
                result = 1;
            else
                result = 0;
        }
        if (op == Operation.BEQ ) {
            if (vj==vk)
                result = 1;
            else
                result = 0;
        }
    }
    public void doSwOperation(int clock){
        this.clock = clock;
        result = vj + A;
    }
}
