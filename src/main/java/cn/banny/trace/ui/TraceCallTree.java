package cn.banny.trace.ui;

import cn.banny.trace.CallNode;
import cn.banny.trace.ThreadInfo;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

class TraceCallTree extends JTree {

    TraceCallTree(ThreadInfo threadInfo, final JTextArea callStackOutput) {
        super(new TraceCallTreeModel(threadInfo));

        this.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                CallNode node = (CallNode) TraceCallTree.this.getLastSelectedPathComponent();
                callStackOutput.setText(node.getStackTraceString());
            }
        });
    }

}
