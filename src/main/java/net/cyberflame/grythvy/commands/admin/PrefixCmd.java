package net.cyberflame.grythvy.commands.admin;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.cyberflame.grythvy.Bot;
import net.cyberflame.grythvy.commands.AdminCommand;
import net.cyberflame.grythvy.settings.Settings;

public class PrefixCmd extends AdminCommand
{
    public PrefixCmd(Bot bot)
    {
        this.name = "prefix";
        this.help = "sets a server-specific prefix";
        this.arguments = "<prefix|NONE>";
        this.aliases = bot.getConfig().getAliases(this.name);
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        if(event.getArgs().isEmpty())
        {
            event.replyError("Please include a prefix or NONE");
            return;
        }
        
        Settings s = event.getClient().getSettingsFor(event.getGuild());
        if(event.getArgs().equalsIgnoreCase("none"))
        {
            s.setPrefix(null);
            event.replySuccess("Prefix cleared.");
        }
        else
        {
            s.setPrefix(event.getArgs());
            event.replySuccess("Custom prefix set to `" + event.getArgs() + "` on *" + event.getGuild().getName() + "*");
        }
    }
}
