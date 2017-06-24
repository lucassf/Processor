package processor;

import java.util.AbstractMap;
import java.util.HashMap;

enum CommandType {
    R, I, J
}

public class Command {

    private static AbstractMap<String, Operation> operationMap;
    public String instruction; //para mostrar na interface depois
    public Operation op;
    public int rs;
    public int rt;
    public int rd;
    public int shamt;
    public int funct;
    public int immediate;
    public int targetAddress;
    public int pc;
    public CommandType commandType;
    public int T_Dest;     //T_Dest é o registrador de destino no Tomasulo.

    public static void setMap() {
        operationMap = new HashMap<>();
        operationMap.put("100000", Operation.ADD);
        operationMap.put("001000", Operation.ADDI);
        operationMap.put("000101", Operation.BEQ);
        operationMap.put("000111", Operation.BLE);
        operationMap.put("000100", Operation.BNE);
        operationMap.put("000010", Operation.JMP);
        operationMap.put("100011", Operation.LW);
        operationMap.put("011000", Operation.MUL);
        operationMap.put("000000", Operation.NOP);
        operationMap.put("100010", Operation.SUB);
        operationMap.put("101011", Operation.SW);
    }

    private static int stringToInt(String val) {
        int ret = 0;
        for (int i = 0; i < val.length(); i++) {
            ret = ret * 2 + val.charAt(i) - '0';
        }
        return ret;
    }

    private static void setRCom(String command, Command com) {
        com.commandType = CommandType.R;
        com.op = operationMap.get(command.substring(26, 32));
        com.rs = stringToInt(command.substring(6, 11));
        com.rt = stringToInt(command.substring(11, 16));
        com.rd = stringToInt(command.substring(16, 21));
        com.shamt = stringToInt(command.substring(21, 26));
        com.funct = stringToInt(command.substring(26, 32));
    }

    private static void setICom(String command, Command com) {
        com.commandType = CommandType.I;
        com.op = operationMap.get(command.substring(0, 6));
        com.rs = stringToInt(command.substring(6, 11));
        com.rt = stringToInt(command.substring(11, 16));
        com.immediate = stringToInt(command.substring(16, 32));
    }

    private static void setJCom(String command, Command com) {
        com.commandType = CommandType.J;
        com.op = operationMap.get(command.substring(0, 6));
        com.targetAddress = stringToInt(command.substring(6, 32));
    }

    public Command() {
        if (operationMap.isEmpty()) {
            setMap();
        }
    }

    public static Command parseCommand(String command, String instrucao) {

        String opcode = command.substring(0, 6);
        Command com = new Command();
        if (opcode.equalsIgnoreCase("000000")) {
            setRCom(command, com);
        } else if (opcode.equalsIgnoreCase("000010")) {
            setJCom(command, com);
        } else {
            setICom(command, com);
        }
        //seta o destino em registrador
        if (com.commandType == CommandType.R) {
            com.T_Dest = com.rd;
        }
        if (com.op == Operation.ADDI || com.op == Operation.LW) {
            com.T_Dest = com.rt;
        }
        com.instruction = instrucao;
        return com;
    }

    public boolean isR() {
        return (commandType == CommandType.R);
    }

    public boolean isI() {
        return (commandType == CommandType.I);
    }

    public boolean isJ() {
        return (commandType == CommandType.J);
    }

    //se pertence a estacao soma
    public boolean isEstacaoSoma() {
        return ((op == Operation.ADD) || (op == Operation.ADDI) || (op == Operation.BEQ)
                || (op == Operation.BLE) || (op == Operation.BNE) || (op == Operation.SUB));
    }

    //se pertence a estacao mult
    public boolean isEstacaoMult() {
        return (op == Operation.MUL);
    }

    //se pertence a estacao memoria
    public boolean isEstacaoMem() {
        return (op == Operation.LW || op == Operation.SW);
    }
}
