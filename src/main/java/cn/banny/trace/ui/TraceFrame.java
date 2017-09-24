package cn.banny.trace.ui;

import javax.swing.*;
import java.awt.*;

class TraceFrame extends JFrame {

    TraceFrame(String title) throws HeadlessException {
        super(title);

        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        Toolkit toolkit = this.getToolkit();
        Dimension screenSize = toolkit.getScreenSize();

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        add(tabbedPane, BorderLayout.CENTER);

        setJMenuBar(new TraceMenuBar(tabbedPane, screenSize));

        setExtendedState(Frame.MAXIMIZED_BOTH);
        setVisible(true);
    }

}
