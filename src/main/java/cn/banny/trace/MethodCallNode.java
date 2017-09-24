package cn.banny.trace;

public interface MethodCallNode extends CallNode {

    MethodCallNode getParent();

    MethodSpec getMethod();

    int getThreadTimeInUsec();

}
