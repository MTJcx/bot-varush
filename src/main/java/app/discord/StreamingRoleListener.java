package app.discord;

import app.utils.BattleriteUtils;
import app.utils.GenericUtils;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.core.events.user.update.UserUpdateGameEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

/**
 * This class listens for changes in Game presence on every user in the server.
 * <p>
 * Purpose of this is to add a role called Streamer to whomever is streaming Battlerite at the moment and remove it
 * as soon as they finish the stream or stop playing Battlerite.
 * <p>
 * To use this create a role called {@value app.utils.GenericUtils#STREAMING_ROLE_NAME}
 * and pin it to the right hand side on your server.
 */
public class StreamingRoleListener extends ListenerAdapter {

    private Role streamerRole;

    /** Constructor, check every member of every guild for to add the Streaming role */
    public StreamingRoleListener(JDA jda) {
        try {
            for (Guild guild : jda.getGuilds())
                for (Member m : guild.getMembers())
                    runChecks(guild, m, m.getGame());
        } catch (Exception ignored) {
        }
    }

    /** Called whenever someone in the server changes their "playing" status */
    @Override
    public void onUserUpdateGame(UserUpdateGameEvent event) {
        super.onUserUpdateGame(event);

        // this isn't in runChecks, so we still remove the role after twitch callback, if the user was given the 
        // probation role while streaming
        if (userHasProbationRole(event.getGuild(), event.getMember()))
            return;

        runChecks(event.getGuild(), event.getMember(), event.getNewGame());
    }

    /**
     * Called whenever someone gets added a new role
     * Useful for when someone joins the server already streaming, so game change wont be detected, but
     * if they add region role then we can add the Streaming role
     */
    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        super.onGuildMemberRoleAdd(event);
        GenericUtils.log(event.getUser().getName() + " added role " + event.getRoles().get(0).getName());

        // this isn't in runChecks, so we still remove the role after twitch callback, if the user was given the 
        // probation role while streaming
        if (userHasProbationRole(event.getGuild(), event.getMember()))
            return;

        if (!event.getRoles().get(0).getName().equals(GenericUtils.STREAMING_ROLE_NAME))
            runChecks(event.getGuild(), event.getMember(), event.getMember().getGame());
    }

    private void runChecks(Guild guild, Member member, Game currentGame) {
        // GenericUtils.log(member.getEffectiveName() + ": running checks");
        try {
            GenericUtils.log(member.getEffectiveName() + " " + currentGame.asRichPresence().getDetails());
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        if (!userHasAtLeastOneRole(member))
            return;

        // stop if there is no role called GenericUtils.SREAMING_ROLE_NAME
        try {
            //TODO: stop intiating the streamer role everytime
            streamerRole = guild.getRolesByName(GenericUtils.STREAMING_ROLE_NAME, true).get(0);
        } catch (Exception e) {
            GenericUtils.log("no streamer role found");
            e.printStackTrace();
            return;
        }

        if (isStreaming(currentGame) && isStreamingBattlerite(currentGame)) {
            GenericUtils.log(member.getEffectiveName() + ": is streaming battlerite");
            addStreamerRole(guild, member);
        } else {
            removeStreamerRole(guild, member);
        }
    }

    /**  Checks if the user is streaming on twitch */
    private boolean isStreaming(Game currentGame) {
        if (currentGame == null || currentGame.getUrl() == null)
            return false;
        return Game.isValidStreamingUrl(currentGame.getUrl());
    }

    /** Checks if the game from Discord rich presence is Battlerite */
    private boolean isStreamingBattlerite(Game currentGame) {
        try {
            return currentGame.asRichPresence().getDetails().equals(BattleriteUtils.BATTLERITE_RICHPRESENCE_NAME);
        } catch (NullPointerException e) {
            return false;
        }
    }

    /** Add the role to the user */
    private void addStreamerRole(Guild guild, Member member) {
        if (!member.getRoles().contains(streamerRole)) {
            GenericUtils.log("add role to " + member);
            guild.getController().addSingleRoleToMember(member, streamerRole).queue();
        }
    }

    /** Check if user has the role and remove it */
    private void removeStreamerRole(Guild guild, Member member) {
        if (member.getRoles().contains(streamerRole)) {
            GenericUtils.log("remove role from " + member);
            guild.getController().removeSingleRoleFromMember(member, streamerRole).queue();
        }
    }

    /** Check if the user as at least one role, which will normally be the region role */
    private boolean userHasAtLeastOneRole(Member member) {
        return !member.getRoles().isEmpty();
    }

    /** Check if user has a "probation" role, if yes then don't add the Streamer role */
    private boolean userHasProbationRole(Guild guild, Member member) {
        try {
            Role probationRole = guild.getRolesByName(GenericUtils.PROBATION_ROLE_NAME, true).get(0);
            return member.getRoles().contains(probationRole);
        } catch (IndexOutOfBoundsException e) {
            GenericUtils.log("no probation role found");
            e.printStackTrace();
            return false;
        }
    }
}