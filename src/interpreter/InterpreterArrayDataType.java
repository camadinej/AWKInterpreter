package interpreter;

import java.util.HashMap;

public class InterpreterArrayDataType extends InterpreterDataType {
    private HashMap<String, InterpreterDataType> arrayData;

    public InterpreterArrayDataType() {
        arrayData = new HashMap<>();
    }
    public InterpreterArrayDataType(String data) {
        super(data);
        arrayData = new HashMap<>();
    }



    public HashMap<String, InterpreterDataType> getArrayData() {
        return arrayData;
    }
}
