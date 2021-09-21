package net.cyberflame.grythvy.commands.owner;

import java.io.IOException;
import java.io.InputStream;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.cyberflame.grythvy.Bot;
import net.cyberflame.grythvy.commands.OwnerCommand;
import net.cyberflame.grythvy.utils.OtherUtil;
import net.dv8tion.jda.api.entities.Icon;

public class SetavatarCmd extends OwnerCommand 
{
    public SetavatarCmd(Bot bot)
    {
        this.name = "setavatar";
        this.help = "sets the avatar of the bot";
        this.arguments = "<url>";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.guildOnly = false;
    }
    
    @Override
    protected void execute(CommandEvent event) 
    {
        String url;
        if(event.getArgs().isEmpty())
            if(!event.getMessage().getAttachments().isEmpty() && event.getMessage().getAttachments().get(0).isImage())
                url = event.getMessage().getAttachments().get(0).getUrl();
            else
                url = null;
        else
            url = event.getArgs();
        InputStream s = OtherUtil.imageFromUrl(url);
        if(s==null)
        {
            event.reply(event.getClient().getError()+" Invalid or missing URL");
        }
        else
        {
            try {
            event.getSelfUser().getManager().setAvatar(Icon.from(s)).queue(
                    v -> event.reply(event.getClient().getSuccess()+" Successfully changed avatar."), 
                    t -> event.reply(event.getClient().getError()+" Failed to set avatar."));
            } catch(IOException e) {
                event.reply(event.getClient().getError()+" Could not load from provided URL.");
            }
        }
    }
}
