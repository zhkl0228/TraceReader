package cn.banny.trace.ui;

import cn.banny.trace.TraceFile;
import cn.banny.trace.TraceReader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

class TraceMenuBar extends JMenuBar {

    TraceMenuBar(final JTabbedPane tabbedPane, final Dimension screenSize) {
        super();

        JMenu file = new JMenu("文件(F)");
        file.setMnemonic(KeyEvent.VK_F);

        JMenuItem open = new JMenuItem("打开(O)");
        open.setMnemonic(KeyEvent.VK_O);
        open.setAccelerator(KeyStroke.getKeyStroke("control O"));
        open.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("选择trace文件");
                fileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return "trace".equalsIgnoreCase(FilenameUtils.getExtension(f.getName()));
                    }
                    @Override
                    public String getDescription() {
                        return "*.trace";
                    }
                });
                if (fileChooser.showOpenDialog(tabbedPane) == JFileChooser.APPROVE_OPTION) {
                    File traceFile = fileChooser.getSelectedFile();
                    if (traceFile != null) {
                        openTraceFile(tabbedPane, screenSize, traceFile);
                    }
                }
            }
        });
        file.add(open);

        JMenuItem close = new JMenuItem("关闭(X)");
        close.setMnemonic(KeyEvent.VK_X);
        close.setAccelerator(KeyStroke.getKeyStroke("control X"));
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Component selected = tabbedPane.getSelectedComponent();
                if (selected != null) {
                    if (selected instanceof Closeable) {
                        IOUtils.closeQuietly((Closeable) selected);
                    }

                    tabbedPane.remove(selected);
                }
            }
        });
        file.add(close);

        add(file);
    }

    private void openTraceFile(JTabbedPane tabbedPane, Dimension screenSize, File file) {
        try {
            TraceFile traceFile = TraceReader.parseTraceFile(file);
            tabbedPane.addTab(FilenameUtils.getBaseName(file.getName()), new TracePanel(traceFile, screenSize));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "打开trace文件出错", JOptionPane.ERROR_MESSAGE);
        }
    }

}
