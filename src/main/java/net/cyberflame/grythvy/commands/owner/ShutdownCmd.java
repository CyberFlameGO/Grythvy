package net.cyberflame.grythvy.commands.owner;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.cyberflame.grythvy.Bot;
import net.cyberflame.grythvy.commands.OwnerCommand;

public class ShutdownCmd extends OwnerCommand
{
    private final Bot bot;
    
    public ShutdownCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "shutdown";
        this.help = "safely shuts down";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }
    
    @Override
    protected void execute(CommandEvent event)
    {
        event.replyWarning("Shutting down...");
        bot.shutdown();
    }
}
