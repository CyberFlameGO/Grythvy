package net.cyberflame.grythvy.commands.dj;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.cyberflame.grythvy.Bot;
import net.cyberflame.grythvy.audio.AudioHandler;
import net.cyberflame.grythvy.audio.RequestMetadata;
import net.cyberflame.grythvy.commands.DJCommand;
import net.dv8tion.jda.api.entities.User;

public class ForceskipCmd extends DJCommand
{
    public ForceskipCmd(Bot bot)
    {
        super(bot);
        this.name = "forceskip";
        this.help = "skips the current song";
        this.aliases = bot.getConfig().getAliases(this.name);
        this.bePlaying = true;
    }

    @Override
    public void doCommand(CommandEvent event) 
    {
        AudioHandler handler = (AudioHandler)event.getGuild().getAudioManager().getSendingHandler();
        RequestMetadata rm = handler.getRequestMetadata();
        event.reply(event.getClient().getSuccess()+" Skipped **"+handler.getPlayer().getPlayingTrack().getInfo().title
                +"** "+(rm.getOwner() == 0L ? "(autoplay)" : "(requested by **" + rm.user.username + "**)"));
        handler.getPlayer().stopTrack();
    }
}
