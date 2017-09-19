package cn.banny.trace;

import java.io.IOException;

public interface MethodCallNode {

    int getMethodId();

    MethodCallNode getParent();

    MethodCallNode[] getChildren() throws IOException;

    MethodSpec getMethod();

    String getStackTraceString();

}
