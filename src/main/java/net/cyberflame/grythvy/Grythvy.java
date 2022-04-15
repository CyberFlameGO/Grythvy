package net.cyberflame.grythvy;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;
import com.jagrosh.jdautilities.examples.command.*;
import net.cyberflame.grythvy.commands.admin.*;
import net.cyberflame.grythvy.commands.dj.*;
import net.cyberflame.grythvy.commands.general.*;
import net.cyberflame.grythvy.commands.music.*;
import net.cyberflame.grythvy.commands.owner.*;
import net.cyberflame.grythvy.entities.Prompt;
import net.cyberflame.grythvy.gui.GUI;
import net.cyberflame.grythvy.settings.SettingsManager;
import java.util.Arrays;
import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Grythvy
{
    public final static Logger LOG = LoggerFactory.getLogger(JMusicBot.class);
    public final static Permission[] RECOMMENDED_PERMS = {Permission.MESSAGE_READ, Permission.MESSAGE_WRITE, Permission.MESSAGE_HISTORY, Permission.MESSAGE_ADD_REACTION,
                                Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_ATTACH_FILES, Permission.MESSAGE_MANAGE, Permission.MESSAGE_EXT_EMOJI,
                                Permission.MANAGE_CHANNEL, Permission.VOICE_CONNECT, Permission.VOICE_SPEAK, Permission.NICKNAME_CHANGE};
    public final static GatewayIntent[] INTENTS = {GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES};
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        if(args.length > 0)
            switch(args[0].toLowerCase())
            {
                case "generate-config":
                    BotConfig.writeDefaultConfig();
                    return;
                default:
            }
        startBot();
    }
    
    private static void startBot()
    {
        // create prompt to handle startup
        Prompt prompt = new Prompt("Grythvy");
        
        // startup checks
        OtherUtil.checkVersion(prompt);
        OtherUtil.checkJavaVersion(prompt);
        
        // load config
        BotConfig config = new BotConfig(prompt);
        config.load();
        if(!config.isValid())
            return;
        LOG.info("Loaded config from " + config.getConfigLocation());
        
        // set up the listener
        EventWaiter waiter = new EventWaiter();
        SettingsManager settings = new SettingsManager();
        Bot bot = new Bot(waiter, config, settings);
        // set up the command client
        CommandClientBuilder cb = new CommandClientBuilder()
                .setPrefix(config.getPrefix())
                .setAlternativePrefix(config.getAltPrefix())
                .setOwnerId(Long.toString(config.getOwnerId()))
                .setEmojis(config.getSuccess(), config.getWarning(), config.getError())
                .setHelpWord(config.getHelp())
                .setLinkedCacheSize(200)
                .setGuildSettingsManager(settings)
                .addCommands(
                        new PingCommand(),
                        new SettingsCmd(bot),
                        
                        new LyricsCmd(bot),
                        new NowplayingCmd(bot),
                        new PlayCmd(bot),
                        new PlaylistsCmd(bot),
                        new QueueCmd(bot),
                        new RemoveCmd(bot),
                        new SearchCmd(bot),
                        new SCSearchCmd(bot),
                        new ShuffleCmd(bot),
                        new SkipCmd(bot),

                        new ForceRemoveCmd(bot),
                        new ForceskipCmd(bot),
                        new MoveTrackCmd(bot),
                        new PauseCmd(bot),
                        new PlaynextCmd(bot),
                        new RepeatCmd(bot),
                        new SkiptoCmd(bot),
                        new StopCmd(bot),
                        new VolumeCmd(bot),
                        
                        new PrefixCmd(bot),
                        new SetdjCmd(bot),
                        new SkipratioCmd(bot),
                        new SettcCmd(bot),
                        new SetvcCmd(bot),
                        
                        new AutoplaylistCmd(bot),
                        new DebugCmd(bot),
                        new PlaylistCmd(bot),
                        new SetavatarCmd(bot),
                        new SetgameCmd(bot),
                        new SetnameCmd(bot),
                        new SetstatusCmd(bot),
                        new ShutdownCmd(bot)
                );
        if(config.useEval())
            cb.addCommand(new EvalCmd(bot));
        boolean nogame = false;
        if(config.getStatus()!=OnlineStatus.UNKNOWN)
            cb.setStatus(config.getStatus());
        if(config.getGame()==null)
            cb.useDefaultGame();
        else if(config.getGame().getName().equalsIgnoreCase("none"))
        {
            cb.setActivity(null);
            nogame = true;
        }
        else
            cb.setActivity(config.getGame());
        
        if(!prompt.isNoGUI())
        {
            try 
            {
                GUI gui = new GUI(bot);
                bot.setGUI(gui);
                gui.init();
            } 
            catch(Exception e) 
            {
                LOG.error("Could not start GUI. If you are "
                        + "running on a server or in a location where you cannot display a "
                        + "window, please run in nogui mode using the -Dnogui=true flag.");
            }
        }
        
        // attempt to log in and start
        try
        {
            JDA jda = JDABuilder.create(config.getToken(), Arrays.asList(INTENTS))
                    .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                    .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.EMOTE, CacheFlag.ONLINE_STATUS)
                    .setActivity(nogame ? null : Activity.playing("loading..."))
                    .setStatus(config.getStatus()==OnlineStatus.INVISIBLE || config.getStatus()==OnlineStatus.OFFLINE 
                            ? OnlineStatus.INVISIBLE : OnlineStatus.DO_NOT_DISTURB)
                    .addEventListeners(cb.build(), waiter, new Listener(bot))
                    .setBulkDeleteSplittingEnabled(true)
                    .build();
            bot.setJDA(jda);
        }
        catch (LoginException ex)
        {
            prompt.alert(Prompt.Level.ERROR, "Grythvy", ex + "\nPlease make sure you are "
                    + "editing the correct config.txt file, and that you have used the "
                    + "correct token (not the 'secret'!)\nConfig Location: " + config.getConfigLocation());
            System.exit(1);
        }
        catch(IllegalArgumentException ex)
        {
            prompt.alert(Prompt.Level.ERROR, "Grythvy", "Some aspect of the configuration is "
                    + "invalid: " + ex + "\nConfig Location: " + config.getConfigLocation());
            System.exit(1);
        }
    }
}
