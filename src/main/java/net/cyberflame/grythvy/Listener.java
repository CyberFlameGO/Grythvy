package net.cyberflame.grythvy;

import net.cyberflame.grythvy.utils.OtherUtil;
import java.util.concurrent.TimeUnit;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Listener extends ListenerAdapter
{
    private final Bot bot;
    
    public Listener(Bot bot)
    {
        this.bot = bot;
    }
    
    @Override
    public void onReady(ReadyEvent event) 
    {
        if(event.getJDA().getGuildCache().isEmpty())
        {
            Logger log = LoggerFactory.getLogger("Grythvy");
            log.warn("This bot is not in any guilds! Use the following link to add the bot to your guilds!");
            log.warn(event.getJDA().getInviteUrl(Grythvy.RECOMMENDED_PERMS));
        }
        event.getJDA().getGuilds().forEach((guild) -> 
        {
            try
            {
                String defpl = bot.getSettingsManager().getSettings(guild).getDefaultPlaylist();
                VoiceChannel vc = bot.getSettingsManager().getSettings(guild).getVoiceChannel(guild);
                if(defpl!=null && vc!=null && bot.getPlayerManager().setUpHandler(guild).playFromDefault())
                {
                    guild.getAudioManager().openAudioConnection(vc);
                }
            }
            catch(Exception ignore) {}
        });
        if(bot.getConfig().useUpdateAlerts())
        {
            bot.getThreadpool().scheduleWithFixedDelay(() -> 
            {
                try
                {
                    User owner = bot.getJDA().retrieveUserById(bot.getConfig().getOwnerId()).complete();
                    String currentVersion = OtherUtil.getCurrentVersion();
                    String latestVersion = OtherUtil.getLatestVersion();
                    if(latestVersion!=null && !currentVersion.equalsIgnoreCase(latestVersion))
                    {
                        String msg = String.format(OtherUtil.NEW_VERSION_AVAILABLE, currentVersion, latestVersion);
                        owner.openPrivateChannel().queue(pc -> pc.sendMessage(msg).queue());
                    }
                }
                catch(Exception ex) {} // ignored
            }, 0, 24, TimeUnit.HOURS);
        }
    }
    
    @Override
    public void onGuildMessageDelete(GuildMessageDeleteEvent event) 
    {
        bot.getNowplayingHandler().onMessageDelete(event.getGuild(), event.getMessageIdLong());
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event)
    {
        bot.getAloneInVoiceHandler().onVoiceUpdate(event);
    }

    @Override
    public void onShutdown(ShutdownEvent event) 
    {
        bot.shutdown();
    }

}
