package cn.banny.trace;

import cn.banny.trace.ui.TraceFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
        UIManager.setLookAndFeel(lookAndFeel);

        new TraceFrame(Main.class.getSimpleName());
    }

}
