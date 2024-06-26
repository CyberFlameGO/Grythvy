package net.cyberflame.grythvy.audio;

import net.cyberflame.grythvy.Bot;
import net.cyberflame.grythvy.entities.Pair;
import net.cyberflame.grythvy.settings.Settings;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.exceptions.RateLimitedException;

public class NowPlayingHandler
{
    private final Bot bot;
    private final HashMap<Long, Pair<Long,Long>> lastNP; // guild -> channel,message
    
    public NowPlayingHandler(Bot bot)
    {
        this.bot = bot;
        this.lastNP = new HashMap<>();
    }
    
    public void init()
    {
        if(!bot.getConfig().useNPImages())
            bot.getThreadpool().scheduleWithFixedDelay(this :: updateAll, 0, 5, TimeUnit.SECONDS);
    }
    
    public void setLastNPMessage(Message m)
    {
        lastNP.put(m.getGuild().getIdLong(), new Pair<>(m.getTextChannel().getIdLong(), m.getIdLong()));
    }
    
    public void clearLastNPMessage(Guild guild)
    {
        lastNP.remove(guild.getIdLong());
    }
    
    private void updateAll()
    {
        Set<Long> toRemove = new HashSet<>();
        for(long guildId: lastNP.keySet())
        {
            Guild guild = bot.getJDA().getGuildById(guildId);
            if(guild==null)
            {
                toRemove.add(guildId);
                continue;
            }
            Pair<Long,Long> pair = lastNP.get(guildId);
            TextChannel tc = guild.getTextChannelById(pair.getKey());
            if(tc==null)
            {
                toRemove.add(guildId);
                continue;
            }
            AudioHandler handler = (AudioHandler)guild.getAudioManager().getSendingHandler();
            assert handler != null;
            Message msg = handler.getNowPlaying(bot.getJDA());
            if(msg==null)
            {
                msg = handler.getNoMusicPlaying(bot.getJDA());
                toRemove.add(guildId);
            }
            try 
            {
                tc.editMessageById(pair.getValue(), msg).queue(m->{}, t -> lastNP.remove(guildId));
            } 
            catch(Exception e) 
            {
                toRemove.add(guildId);
            }
        }
        toRemove.forEach(lastNP :: remove);
    }

    // "event"-based methods
    public void onTrackUpdate(AudioTrack track)
    {
        // update bot status if applicable
        if(bot.getConfig().getSongInStatus())
        {
            if(track!=null && bot.getJDA().getGuilds().stream().filter(g -> Objects
                    .requireNonNull(g.getSelfMember().getVoiceState()).inVoiceChannel()).count() <= 1)
                bot.getJDA().getPresence().setActivity(Activity.listening(track.getInfo().title));
            else
                bot.resetGame();
        }
    }
    
    public void onMessageDelete(Guild guild, long messageId)
    {
        Pair<Long,Long> pair = lastNP.get(guild.getIdLong());
        if(pair==null)
            return;
        if(pair.getValue() == messageId)
            lastNP.remove(guild.getIdLong());
    }
}
