package cn.banny.trace.ui;

import cn.banny.trace.ThreadInfo;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ThreadsComboBoxModel implements ComboBoxModel<ThreadInfo> {

    private final ThreadInfo[] threads;

    ThreadsComboBoxModel(List<ThreadInfo> threads) {
        super();

        List<ThreadInfo> list = new ArrayList<>(threads.size() + 1);
        list.add(new SelectThreadInfo());
        list.addAll(threads);
        Collections.sort(list, new Comparator<ThreadInfo>() {
            @Override
            public int compare(ThreadInfo o1, ThreadInfo o2) {
                return o1.getThreadId() - o2.getThreadId();
            }
        });
        this.threads = list.toArray(new ThreadInfo[0]);
    }

    private Object selectedItem;

    public void setSelectedItem(Object anItem) {
        selectedItem = anItem;
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

    public int getSize() {
        return threads.length;
    }

    public ThreadInfo getElementAt(int index) {
        return threads[index];
    }

    public void addListDataListener(ListDataListener l) {
    }

    public void removeListDataListener(ListDataListener l) {
    }

}
