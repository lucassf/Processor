package processor;

import java.util.ArrayList;
import java.util.List;

enum StationType{
    ADD, MULT, MEM
}

class ReservationStation {

    public String name;
    public String instruction;
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
        this.instruction = r.instruction;
    }

    public ReservationStation(Processor proc,String name) {
        this.name = name;
        busy = false;
        op = Operation.EMPTY;
        dest = vj = vk = qj = qk = A = -1;
        this.proc = proc;
    }

    public void clear() {
        name = "";
        busy = false;
        op = Operation.EMPTY;
        dest = vj = vk = qj = qk = A = -1;
    }

    public boolean inserirComando(Command co) {

        //encontar um rob nao ocupado
        int b = proc.getFirstNonBusyRob();
        if (b == -1){
            return false;     //nenhum rob disponivel
        }
        List<Register> regs = proc.getR();
        List<Register> rTemp = proc.getRegTemp();
        ArrayList<ReorderBuffer> rob = proc.getRob();
        ArrayList<ReorderBuffer> robTemp = proc.getRobTemp();

        instruction = co.instruction;
        
        int rd = co.rd;
        int rt = co.rt;
        int rs = co.rs;

        robTemp.get(b).instruction = co.instruction;
        robTemp.get(b).ready = false;
        robTemp.get(b).busy = true;

        //tem operandos rs e rt
        if (co.isR() || co.isI()) {
            //se alguma instrucao grava em rs
            if (regs.get(rs).busy) {
                int h = regs.get(rs).qi;
                if (rob.get(h).ready) {//inst ja concluida
                    vj = rob.get(h).value;
                    qj = -1;
                } else {
                    vj = -1;
                    qj = h;
                }
            } else {
                vj = regs.get(rs).value;
                qj = -1;
            }
            busy = true;
            dest = b;
        }

        //se alguma instrucao grava em rt
        if (co.isR() || co.op == Operation.BEQ || co.op == Operation.BLE || co.op == Operation.BNE) {
            if (regs.get(rt).busy) {
                int h = regs.get(rt).qi;
                if (rob.get(h).ready) {//inst ja concluida
                    vk = rob.get(h).value;
                    qk = -1;
                } else {
                    qk = h;
                    vk = -1;
                }
            } else {
                vk = regs.get(rt).value;
                qk = -1;
            }
        } else {
            vk = regs.get(co.rt).value;
            qk = -1;
        }

        if (co.commandType == CommandType.R) {
            //grava em rd
            rTemp.get(rd).qi = b;
            rTemp.get(rd).busy = true;
            robTemp.get(b).destination = rd;
        }
        //caso addi rt = rs + imm
        //load r[rt] = MEM[r[rs] + imm]]
        if (co.op == Operation.ADDI || co.op == Operation.LW) {
            robTemp.get(b).destination = rt;
            //immediate
            vk = co.immediate;
            qk = -1;
            //rt
            rTemp.get(co.rt).qi = b;
            rTemp.get(co.rt).busy = true;
        }
        if (co.op == Operation.SW || co.op == Operation.LW) {
            A = co.immediate;
        }
        
        //Get the operation
        this.op = co.op;
        
        return true;
    }
}
