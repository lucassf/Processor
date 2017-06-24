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

    public int nonBusyClock = -1;
    public boolean toWrite = false;
    public int result = -1;// branch: 0 se nao jump, 1 se jump
    public int timeToFinish;
    public int vj = -1;
    public int vk = -1; 
    public int A = -1;
    public Operation op;
    public ReservationStation station;
    
    public ULA(int i) {
        this.timeToFinish = i;
        clear();
    }
    
    public void clear() {
        result = -1;
        vj = -1;
        vk = -1; 
        A = -1;
        nonBusyClock = -1;
        toWrite = false;
    }

    public void doFPOperation(int clock) {
        this.nonBusyClock = clock+1;
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
        this.nonBusyClock = clock+1;
        result = vj + A;
    }
    
    public boolean isBusy(int curClock){
        return curClock < nonBusyClock;
    }
}
