package net.cyberflame.grythvy.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.cyberflame.grythvy.queue.Queueable;
import net.cyberflame.grythvy.utils.FormatUtil;
import net.dv8tion.jda.api.entities.User;

public class QueuedTrack implements Queueable
{
    private final AudioTrack track;
    
    public QueuedTrack(AudioTrack track, User owner)
    {
        this(track, new RequestMetadata(owner));
    }
    
    public QueuedTrack(AudioTrack track, RequestMetadata rm)
    {
        this.track = track;
        this.track.setUserData(rm);
    }
    
    @Override
    public long getIdentifier() 
    {
        return track.getUserData(RequestMetadata.class).getOwner();
    }
    
    public AudioTrack getTrack()
    {
        return track;
    }

    @Override
    public String toString() 
    {
        return "`[" + FormatUtil.formatTime(track.getDuration()) + "]` **" + track.getInfo().title + "** - <@" + track.getUserData(RequestMetadata.class).getOwner() + ">";
    }
}
