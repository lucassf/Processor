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
    public int stationIndex = -1;//estacao de reserva em execucao
    public Operation op;
   // public ReservationStation rStationOperando = null;

    public ULA(int i) {
        this.timeToFinish = i;
    }

    public void doFPOperation() {
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
    public void doSwOperation(){
        result = vj + A;
    }
}
