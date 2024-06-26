package net.cyberflame.grythvy.commands;

import com.jagrosh.jdautilities.command.CommandEvent;
import net.cyberflame.grythvy.Bot;
import net.cyberflame.grythvy.settings.Settings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Role;

public abstract class DJCommand extends MusicCommand
{
    public DJCommand(Bot bot)
    {
        super(bot);
        this.category = new Category("DJ", DJCommand :: checkDJPermission);
    }
    
    public static boolean checkDJPermission(CommandEvent event)
    {
        if(event.getAuthor().getId().equals(event.getClient().getOwnerId()))
            return true;
        if(event.getGuild()==null)
            return true;
        if(event.getMember().hasPermission(Permission.MANAGE_SERVER))
            return true;
        Settings settings = event.getClient().getSettingsFor(event.getGuild());
        Role dj = settings.getRole(event.getGuild());
        return dj!=null && (event.getMember().getRoles().contains(dj) || dj.getIdLong()==event.getGuild().getIdLong());
    }
}
