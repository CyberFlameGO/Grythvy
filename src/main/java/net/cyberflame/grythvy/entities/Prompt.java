package net.cyberflame.grythvy.entities;

import java.util.Scanner;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Prompt
{
    private final String title;
    private final String noguiMessage;
    
    private boolean nogui;
    private final boolean noprompt;
    private Scanner scanner;
    
    public Prompt(String title)
    {
        this(title, null);
    }
    
    public Prompt(String title, String noguiMessage)
    {
        this(title, noguiMessage, "true".equalsIgnoreCase(System.getProperty("nogui")), "true".equalsIgnoreCase(System.getProperty("noprompt")));
    }
    
    public Prompt(String title, String noguiMessage, boolean nogui, boolean noprompt)
    {
        this.title = title;
        this.noguiMessage = noguiMessage == null ? "Switching to nogui mode. You can manually start in nogui mode by including the -Dnogui=true flag." : noguiMessage;
        this.nogui = nogui;
        this.noprompt = noprompt;
    }
    
    public boolean isNoGUI()
    {
        return nogui;
    }
    
    public void alert(Level level, String context, String message)
    {
        if(nogui)
        {
            Logger log = LoggerFactory.getLogger(context);
            switch (level)
                {
                    case WARNING -> log.warn(message);
                    case ERROR -> log.error(message);
                    default -> log.info(message);
                }
        }
        else
        {
            try 
            {
                int option = switch (level)
                        {
                            case INFO -> JOptionPane.INFORMATION_MESSAGE;
                            case WARNING -> JOptionPane.WARNING_MESSAGE;
                            case ERROR -> JOptionPane.ERROR_MESSAGE;
                            //noinspection UnnecessaryDefault
                            default -> JOptionPane.PLAIN_MESSAGE;
                        };
                //noinspection MagicConstant
                JOptionPane.showMessageDialog(null, "<html><body><p style='width: 400px;'>"+message, title, option);
            }
            catch(Exception e) 
            {
                nogui = true;
                alert(Level.WARNING, context, noguiMessage);
                alert(level, context, message);
            }
        }
    }
    
    public String prompt(String content)
    {
        if(noprompt)
            return null;
        if(nogui)
        {
            if(scanner==null)
                scanner = new Scanner(System.in);
            try
            {
                System.out.println(content);
                if(scanner.hasNextLine())
                    return scanner.nextLine();
                return null;
            }
            catch(Exception e)
            {
                alert(Level.ERROR, title, "Unable to read input from command line.");
                e.printStackTrace();
                return null;
            }
        }
        else
        {
            try 
            {
                return JOptionPane.showInputDialog(null, content, title, JOptionPane.QUESTION_MESSAGE);
            }
            catch(Exception e) 
            {
                nogui = true;
                alert(Level.WARNING, title, noguiMessage);
                return prompt(content);
            }
        }
    }
    
    public enum Level
    {
        INFO, WARNING, ERROR
    }
}
