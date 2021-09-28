package net.cyberflame.grythvy.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.cyberflame.grythvy.Bot;
import net.cyberflame.grythvy.commands.DJCommand;
import net.cyberflame.grythvy.settings.RepeatMode;
import net.cyberflame.grythvy.settings.Settings;

public class RepeatCmd extends DJCommand
{
    public RepeatCmd(Bot bot)
    {
        super(bot);
        this.name = "repeat";
        this.help = "re-adds music to the queue when finished";
        this.arguments = "[off|all|single]";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = true;
    }
    
    // override musiccommand's execute because we don't actually care where this is used
    @Override
    protected void execute(CommandEvent event) 
    {
        String args = event.getArgs();
        RepeatMode value;
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        if(args.isEmpty())
        {
            if(settings.getRepeatMode() == RepeatMode.OFF)
                value = RepeatMode.ALL;
            else
                value = RepeatMode.OFF;
        }
        else if(args.equalsIgnoreCase("false") || args.equalsIgnoreCase("off"))
        {
            value = RepeatMode.OFF;
        }
        else if(args.equalsIgnoreCase("true") || args.equalsIgnoreCase("on") || args.equalsIgnoreCase("all"))
        {
            value = RepeatMode.ALL;
        }
        else if(args.equalsIgnoreCase("one") || args.equalsIgnoreCase("single"))
        {
            value = RepeatMode.SINGLE;
        }
        else
        {
            event.replyError("Valid options are `off`, `all` or `single` (or leave empty to toggle between `off` and `all`)");
            return;
        }
        settings.setRepeatMode(value);
        event.replySuccess("Repeat mode is now `"+value.getUserFriendlyName()+"`");
    }

    @Override
    public void doCommand(CommandEvent event) { /* Intentionally Empty */ }
}
