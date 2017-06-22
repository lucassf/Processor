/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processor;

import java.util.ArrayList;
import java.util.List;

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
    public Processor proc;

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
        this.proc = r.proc;
    }

    public ReservationStation(Processor proc) {
        name = "";
        busy = false;
        op = Operation.EMPTY;
        dest = vj = vk = qj = qk = A = -1;
        this.proc = proc;
    }
   
    public void inserirComando(Command co) {

        //encontar um rob nao ocupado
        int b = proc.getFirstNonBusyRob();
        List<Register> regs = proc.getRegisters();
        List<Register> rTemp = proc.getRegTemp();
        ArrayList<ReorderBuffer> rob = proc.getRob();
        ArrayList<ReorderBuffer> robTemp = proc.getRobTemp();

        name = co.name;
        //tem operandos rs e rt
        if (co.commandType == CommandType.R || co.op == Operation.BEQ
                || co.op == Operation.BLE || co.op == Operation.BNE || co.op == Operation.LW) {
            //se alguma instrucao grava em rs
            if (regs.get(co.rs).busy) {
                int h = regs.get(co.rs).qi;
                if (rob.get(h).ready) {//inst ja concluida
                    vj = rob.get(h).value;
                    qj = 0;
                }
                else {
                    qj = h;
                }
            } else {
                vj = regs.get(co.rs).value;
                qj = 0;
            }
            busy = true;
            dest = b;
            robTemp.get(b).instruction = co.name;
            robTemp.get(b).destination = co.rd;
            robTemp.get(b).ready = false;
            
            //INTRUCAO R: R[rd] = R[rs] op R[rt]
            //Necessita gravar, bloqueia o registrador destino
            if (co.commandType == CommandType.R) {
                //grava em rd
                rTemp.get(co.rd).qi = b;
                rTemp.get(co.rd).busy = true;
            }
        
            //se alguma instrucao grava em rt
            if (regs.get(co.rt).busy) {
                int h = regs.get(co.rt).qi;
                if (rob.get(h).ready) {//inst ja concluida
                    vk = rob.get(h).value;
                    qk = 0;
                } else {
                    qk = h;
                }
            } else {
                vk = regs.get(co.rt).value;
                qk = -1;
            }
        }
        //caso addi rt = rs + imm
        //load r[rt] = MEM[r[rs] + imm]]
        if (co.op == Operation.ADDI || co.op == Operation.LW) {
            if (regs.get(co.rs).busy) {
                int h = regs.get(co.rs).qi;
                if (rob.get(h).ready) {//inst ja concluida
                    vj = rob.get(h).value;
                    qj = 0;
                } else {
                    qj = h;
                }
            } else {
                vj = regs.get(co.rs).value;
                qj = 0;
            }
            busy = true;
            dest = b;
            robTemp.get(b).instruction = co.name;
            robTemp.get(b).destination = co.rt;
            robTemp.get(b).ready = false;
            //immediate
            vk = co.immediate;
            qk = 0;
            //rt
            rTemp.get(co.rt).qi = b;
            rTemp.get(co.rt).busy = true;
        }
        if (co.op == Operation.SW || co.op == Operation.LW) {
            A = co.immediate;
        }
    }
}
