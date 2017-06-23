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
    public void clear(){
        name = "";
        busy = false;
        op = Operation.EMPTY;
        dest = vj = vk = qj = qk = A = -1;
    }
    public void inserirComando(Command command) {

        //encontar um rob nao ocupado
        int b = proc.getFirstNonBusyRob();
        if (b == -1) {
            return;     //rob nao encontrado
        }
        List<Register> regs = proc.getRegisters();
        List<Register> rTemp = proc.getRegTemp();
        ArrayList<ReorderBuffer> rob = proc.getRob();
        ArrayList<ReorderBuffer> robTemp = proc.getRobTemp();
        name = command.name;
        
        //tomasulo issue, todas as instruções
        if (regs.get(command.rs).busy) {
            int h = regs.get(command.rs).qi;
            if (rob.get(h).ready) {//inst ja concluida
                vj = rob.get(h).value;
                qj = 0;
            } else {
                qj = h;
            }
        } else {
            vj = regs.get(command.rs).value;
            qj = 0;
        }
        busy = true;
        dest = b;
        robTemp.get(b).instruction = command.name;
        robTemp.get(b).destination = command.rd;
        robTemp.get(b).ready = false;

        //INTRUCAO R: R[rd] = R[rs] op R[rt]
        //Necessita gravar, bloqueia o registrador destino
        if (command.commandType == CommandType.R) {
            //grava em rd
            rTemp.get(command.rd).qi = b;
            rTemp.get(command.rd).busy = true;
        }

        //se alguma instrucao grava em rt
        if (regs.get(command.rt).busy) {
            int h = regs.get(command.rt).qi;
            if (rob.get(h).ready) {//inst ja concluida
                vk = rob.get(h).value;
                qk = 0;
            } else {
                qk = h;
            }
        } else {
            vk = regs.get(command.rt).value;
            qk = -1;
        }
            
        //caso addi rt = rs + imm
        //load r[rt] = MEM[r[rs] + imm]]
        if (command.op == Operation.ADDI || command.op == Operation.LW) {
            if (regs.get(command.rs).busy) {
                int h = regs.get(command.rs).qi;
                if (rob.get(h).ready) {//inst ja concluida
                    vj = rob.get(h).value;
                    qj = 0;
                } else {
                    qj = h;
                }
            } else {
                vj = regs.get(command.rs).value;
                qj = 0;
            }
            busy = true;
            dest = b;
            robTemp.get(b).instruction = command.name;
            robTemp.get(b).destination = command.rt;
            robTemp.get(b).ready = false;
            //immediate
            vk = command.immediate;
            qk = 0;
            //rt
            rTemp.get(command.rt).qi = b;
            rTemp.get(command.rt).busy = true;
        }
        if (command.op == Operation.SW || command.op == Operation.LW) {
            A = command.immediate;
        }
    }
}
