package processor;

import java.util.AbstractMap;

public class CommandParser {
    
    AbstractMap<String,Operation> operationMap;
    
    public CommandParser(){
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
    
    public Operation parseCommand(String command){
        
        Operation op;
        String opcode = command.substring(0,6);
        if (opcode.equalsIgnoreCase("000000")){
            String funct = command.substring(27,command.length());
            op = operationMap.get(funct);
        }else{
            op = operationMap.get("opcode");
        }
        return op;
    }
}
