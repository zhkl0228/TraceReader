package cn.banny.trace.ui;

import cn.banny.trace.ThreadInfo;
import cn.banny.trace.TraceFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

class TracePanel extends JPanel {

    TracePanel(TraceFile traceFile, final Dimension screenSize) {
        super(new BorderLayout());

        final JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(screenSize.width - 600);

        final JLabel tip = new JLabel("");
        splitPane.setLeftComponent(tip);

        final JTextArea callStackOutput = new JTextArea();
        callStackOutput.setEditable(false);
        splitPane.setRightComponent(callStackOutput);
        this.add(splitPane, BorderLayout.CENTER);

        JComboBox<ThreadInfo> threads = new JComboBox<>();
        threads.setModel(new ThreadsComboBoxModel(traceFile.getThreads()));
        threads.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                callStackOutput.setText("");
                ThreadInfo threadInfo = (ThreadInfo) e.getItem();
                if (threadInfo instanceof SelectThreadInfo) {
                    splitPane.setLeftComponent(tip);
                } else if (threadInfo != null) {
                    splitPane.setDividerLocation(screenSize.width - 600);
                    splitPane.setLeftComponent(new TraceCallTree(threadInfo, callStackOutput));
                }
            }
        });
        threads.setSelectedIndex(0);
        this.add(threads, BorderLayout.NORTH);
    }
}
