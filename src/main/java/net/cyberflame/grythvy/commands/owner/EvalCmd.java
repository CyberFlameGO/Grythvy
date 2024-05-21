package net.cyberflame.grythvy.commands.owner;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.cyberflame.grythvy.Bot;
import net.cyberflame.grythvy.commands.OwnerCommand;
import net.dv8tion.jda.api.entities.ChannelType;

public class EvalCmd extends OwnerCommand 
{
    private final Bot bot;
    private final String engine;
    
    public EvalCmd(Bot bot)
    {
        this.bot = bot;
        this.name = "eval";
        this.help = "evaluates nashorn code";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.engine = bot.getConfig().getEvalEngine();
        this.guildOnly = false;
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        ScriptEngine se = new ScriptEngineManager().getEngineByName(engine);
        if(se == null)
        {
            event.replyError("The eval engine provided in the config (`"+engine+"`) doesn't exist. This could be due to an invalid "
                    + "engine name, or the engine not existing in your version of java (`"+System.getProperty("java.version")+"`).");
            return;
        }
        se.put("bot", bot);
        se.put("event", event);
        se.put("jda", event.getJDA());
        if (event.getChannelType() != ChannelType.PRIVATE) {
            se.put("guild", event.getGuild());
            se.put("channel", event.getChannel());
        }
        try
        {
            event.reply(event.getClient().getSuccess()+" Evaluated Successfully:\n```\n"+se.eval(event.getArgs())+" ```");
        } 
        catch(Exception e)
        {
            event.reply(event.getClient().getError()+" An exception was thrown:\n```\n"+e+" ```");
        }
    }
    
}
