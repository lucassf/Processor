package processor;

import java.util.AbstractMap;

enum CommandType{
    R,I,J
}

public class Command {
    
    private static AbstractMap<String,Operation> operationMap;
    
    public Operation op;
    public int rs;
    public int rt;
    public int rd;
    public int shamt;
    public int immediate;
    public int targetAddress;
    public CommandType commandType;
    
    public static void setMap(){
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
    
    private static int stringToInt(String val){
        int ret = 0;
        for (int i=0;i<val.length();i++){
            ret = ret*2+val.charAt(i)-'0';
        }
        return ret;
    }
    
    private static void setRCom(String command,Command com){
        com.commandType = CommandType.R;
        com.op = operationMap.get(command.substring(26));
        com.rs = stringToInt(command.substring(6,11));
        com.rt = stringToInt(command.substring(11,16));
        com.rd = stringToInt(command.substring(16,21));
        com.shamt = stringToInt(command.substring(21,26));
    }
    
    private static void setICom(String command,Command com){
        com.commandType = CommandType.I;
        com.op = operationMap.get(command.substring(0,6));
        com.rs = stringToInt(command.substring(6,11));
        com.rt = stringToInt(command.substring(11,16));
        com.immediate = stringToInt(command.substring(16));
    }
    
    private static void setJCom(String command,Command com){
        com.commandType = CommandType.J;
        com.op = operationMap.get(command.substring(0,6));
        com.targetAddress = stringToInt(command.substring(6));
    }
    
    public Command(){
        if (operationMap.isEmpty())setMap();
    }
    
    public static Command parseCommand(String command){
        
        String opcode = command.substring(0,6);
        Command com = new Command();
        if (opcode.equalsIgnoreCase("000000")){
            setICom(command,com);
        }else if (opcode.equalsIgnoreCase("000010")){
            setJCom(command,com);
        }
        else{
            setICom(command,com);
        }
        return com;
    }
}
