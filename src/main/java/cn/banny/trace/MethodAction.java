package cn.banny.trace;

public enum MethodAction {

    METHOD_TRACE_ENTER, // method entry

    METHOD_TRACE_EXIT, // method exit

    METHOD_TRACE_UNROLL; // method exited by exception unrolling

    public static MethodAction decodeAction(int value) {
        for (MethodAction action : MethodAction.values()) {
            if (action.ordinal() == value) {
                return action;
            }
        }
        throw new IllegalStateException("decodeAction failed: " + value);
    }

}
