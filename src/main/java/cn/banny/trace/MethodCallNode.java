package cn.banny.trace;

import java.io.IOException;

public interface MethodCallNode {

    MethodCallNode getParent();

    MethodCallNode[] getChildren() throws IOException;

    MethodSpec getMethod();

    String getStackTraceString();

    int getThreadTimeInUsec();

}
