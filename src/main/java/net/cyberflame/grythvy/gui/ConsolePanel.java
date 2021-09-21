package net.cyberflame.grythvy.gui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.PrintStream;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ConsolePanel extends JPanel {
    
    public ConsolePanel()
    {
        super();
        JTextArea text = new JTextArea();
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setEditable(false);
        PrintStream con=new PrintStream(new TextAreaOutputStream(text));
        System.setOut(con);
        System.setErr(con);
        
        JScrollPane pane = new JScrollPane();
        pane.setViewportView(text);
        
        super.setLayout(new GridLayout(1,1));
        super.add(pane);
        super.setPreferredSize(new Dimension(400,300));
    }
}
