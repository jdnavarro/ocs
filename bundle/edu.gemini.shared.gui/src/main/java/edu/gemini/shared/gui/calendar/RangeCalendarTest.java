package edu.gemini.shared.gui.calendar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class RangeCalendarTest {

    public static void main(String args[]) {
        JFrame frame = new JFrame();
        frame.setTitle("RangeCalendar Test Harness");
        frame.setBounds(50, 100, 350, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
        RangeCalendar cm = new RangeCalendar(new DefaultCalendarModel(1999, 0, 1999, 8), 3);
        cm.setDisplayMode(CalendarMonth.MULTI_MONTH_MODE);
        DefaultRangeCellRenderer r = new DefaultRangeCellRenderer(cm.getRangeCount());
        r.setBackground1(AbstractCalendarRenderer.DEFAULT_MULTI_MONTH_BACKGROUND1);
        r.setBackground2(AbstractCalendarRenderer.DEFAULT_MULTI_MONTH_BACKGROUND2);
        cm.setCellRenderer(r);
        JCalendar cv = new JCalendar(cm, true, true, true, true);

        JPanel pan = new JPanel(new BorderLayout());
        pan.add(cv);
        frame.getContentPane().add("Center", pan);

        //frame.pack();
        frame.setVisible(true);
    }

}

