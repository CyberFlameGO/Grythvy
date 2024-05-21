package net.cyberflame.grythvy.audio;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.cyberflame.grythvy.utils.TimeUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author John Grosh (john.a.grosh@gmail.com)
 */
public class RequestMetadata
{
    public static final RequestMetadata EMPTY = new RequestMetadata(null, null);
    
    public final UserInfo user;
    public final RequestInfo requestInfo;
    
    public RequestMetadata(User user, RequestInfo requestInfo)
    {
        this.user = user == null ? null : new UserInfo(user.getIdLong(), user.getName(), user.getDiscriminator(), user.getEffectiveAvatarUrl());
        this.requestInfo = requestInfo;
    }
    
    public long getOwner()
    {
        return user == null ? 0L : user.id;
    }

    public static RequestMetadata fromResultHandler(AudioTrack track, CommandEvent event)
    {
        return new RequestMetadata(event.getAuthor(), new RequestInfo(event.getArgs(), track.getInfo().uri));
    }
    
    public static class RequestInfo
    {
        public final String query, url;
        public final long startTimestamp;

        public RequestInfo(String query, String url)
        {
            this(query, url, tryGetTimestamp(query));
        }

        private RequestInfo(String query, String url, long startTimestamp)
        {
            this.url = url;
            this.query = query;
            this.startTimestamp = startTimestamp;
        }

        private static final Pattern youtubeTimestampPattern = Pattern.compile("youtu(?:\\.be|be\\..+)/.*\\?.*(?!.*list=)t=([\\dhms]+)");
        private static long tryGetTimestamp(String url)
        {
            Matcher matcher = youtubeTimestampPattern.matcher(url);
            return matcher.find() ? TimeUtil.parseUnitTime(matcher.group(1)) : 0;
        }
    }
    
    public static class UserInfo
    {
        public final long id;
        public final String username, discrim, avatar;
        
        private UserInfo(long id, String username, String discrim, String avatar)
        {
            this.id = id;
            this.username = username;
            this.discrim = discrim;
            this.avatar = avatar;
        }
    }
}
