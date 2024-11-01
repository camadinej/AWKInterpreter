package interpreter;

public class ReturnType {
    public enum FlowControlStatement {BREAK, CONTINUE, RETURN, NONE};

    private FlowControlStatement flowControlType;

    private String returnValue;

    public ReturnType(FlowControlStatement flowControlType) {
        this.flowControlType = flowControlType;
        returnValue = "";
    }
    public ReturnType(FlowControlStatement flowControlType, String returnValue) {
        this(flowControlType);
        this.returnValue = returnValue;
    }
    public FlowControlStatement getFlowControlType() {
        return flowControlType;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public String toString() {
        if(returnValue != null) {
            return "flow control type: " + flowControlType + " retVal: "  + returnValue;
        }
        return "flow control type: " + flowControlType;
    }
}
