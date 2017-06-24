package processor;

import java.util.ArrayList;
import java.util.List;

class ReservationStation {

    public String name;
    public String instruction;
    private int nonBusyClock;
    public int issuedClock;
    public int clockInserted;
    public Operation op;
    public int vj;
    public int vk;
    public ReorderBuffer qj;
    public ReorderBuffer qk;
    public int A;
    private Processor proc;
    public ReorderBuffer reorder;
    public int id;

    public ReservationStation(Processor proc, String name, int id) {
        clear();
        this.name = name;
        this.proc = proc;
        this.id = id;
    }
    
    public boolean isBusy(int curClock){
        return curClock < nonBusyClock;
    }

    public void clear() {
        nonBusyClock = -1;
        op = Operation.EMPTY;
        vj = vk = A = -1;
        qj = qk = null;
        reorder = null;
    }

    public boolean inserirComando(Command command, int clock) {

        //encontar um rob nao ocupado
        ReorderBuffer r = proc.getFirstNonBusyRob();
        if (r == null){
            System.out.println("Tried issue, no ROB available");
            return false;     //nenhum rob disponivel
        }
        
        reorder = r;
        clockInserted = clock;
        issuedClock = clock;
        
        List<Register> regs = proc.getR();
        ArrayList<ReorderBuffer> rob = proc.getRob();

        instruction = command.instruction;
        
        int rd = command.rd;
        int rt = command.rt;
        int rs = command.rs;

        r.instruction = command.instruction;
        r.ready = false;
        r.nonBusyClock = Integer.MAX_VALUE;

        //tem operandos rs e rt
        if (command.isR() || command.isI()) {
            //se alguma instrucao grava em rs
            if (regs.get(rs).busy) {
                ReorderBuffer h = regs.get(rs).qi;
                if (h.ready) {//inst ja concluida
                    vj = h.value;
                    qj = null;
                }
                else {
                    vj = -1;
                    qj = h;
                }
            } else {
                vj = regs.get(rs).value;
                qj = null;
            }
            nonBusyClock = Integer.MAX_VALUE;
        }

        //se alguma instrucao grava em rt
        if (command.isR() || command.op == Operation.BEQ || command.op == Operation.BLE || command.op == Operation.BNE) {
            if (regs.get(rt).busy) {
                ReorderBuffer h = regs.get(rt).qi;
                if (h.ready) {//inst ja concluida
                    vk = h.value;
                    qk = null;
                } else {
                    qk = h;
                    vk = -1;
                }
            } else {
                vk = regs.get(rt).value;
                qk = null;
            }
        } else {
            vk = regs.get(command.rt).value;
            qk = null;
        }

        if (command.commandType == CommandType.R) {
            //grava em rd
            regs.get(rd).qi = reorder;
            regs.get(rd).busy = true;
            r.destination = rd;
        }
        //caso addi rt = rs + imm
        //load r[rt] = MEM[r[rs] + imm]]
        if (command.op == Operation.ADDI || command.op == Operation.LW) {
            r.destination = rt;
            //immediate
            vk = command.immediate;
            qk = null;
            //rt
            regs.get(command.rt).qi = reorder;
            regs.get(command.rt).busy = true;
        }
        if (command.op == Operation.SW || command.op == Operation.LW) {
            A = command.immediate;
        }
        
        //Get the operation
        this.op = command.op;
        
        return true;
    }
}
