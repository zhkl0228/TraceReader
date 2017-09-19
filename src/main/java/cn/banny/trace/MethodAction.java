package cn.banny.trace;

public enum MethodAction {

    ENTER,

    EXIT,

    EXCEPTION;

    public static MethodAction decodeAction(int value) {
        for (MethodAction action : MethodAction.values()) {
            if (action.ordinal() == value) {
                return action;
            }
        }
        throw new IllegalStateException("decodeAction failed: " + value);
    }

}
