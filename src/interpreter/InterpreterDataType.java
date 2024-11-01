package interpreter;

public class InterpreterDataType {
    private String data;

    public InterpreterDataType() {
        data = "";
    }
    public InterpreterDataType(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }
}
