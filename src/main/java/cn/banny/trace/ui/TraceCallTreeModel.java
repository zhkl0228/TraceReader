package cn.banny.trace.ui;

import cn.banny.trace.CallNode;
import cn.banny.trace.ThreadInfo;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.Arrays;

public class TraceCallTreeModel implements TreeModel {

    private final ThreadInfo threadInfo;

    TraceCallTreeModel(ThreadInfo threadInfo) {
        super();
        this.threadInfo = threadInfo;
    }

    @Override
    public Object getRoot() {
        return threadInfo;
    }

    @Override
    public Object getChild(Object parent, int index) {
        CallNode node = (CallNode) parent;
        return node.getChildren()[index];
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        CallNode node = (CallNode) parent;
        return Arrays.binarySearch(node.getChildren(), child);
    }

    @Override
    public int getChildCount(Object parent) {
        CallNode node = (CallNode) parent;
        return node.getChildren().length;
    }

    @Override
    public boolean isLeaf(Object node) {
        CallNode callNode = (CallNode) node;
        return callNode.getChildren().length < 1;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
    }

}
