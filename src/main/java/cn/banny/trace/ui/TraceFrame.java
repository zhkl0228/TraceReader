package cn.banny.trace.ui;

import javax.swing.*;
import java.awt.*;

public class TraceFrame extends JFrame {

    public TraceFrame(String title) throws HeadlessException {
        super(title);

        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        Toolkit toolkit = this.getToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        this.setSize(screenSize);
        this.setResizable(false);

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        add(tabbedPane, BorderLayout.CENTER);

        setJMenuBar(new TraceMenuBar(tabbedPane, screenSize));

        setVisible(true);
    }

}
