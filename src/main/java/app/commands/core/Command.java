package app.commands.core;

import java.util.ArrayList;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.MessageChannel;

/**
 * Base class for every command
 */

public abstract class Command {

    public interface CommandEventsListener {
        void onMessageUpdated(String discordMessageId, EmbedBuilder eb);
    }

    /** Key is the first keyword after the trigger and is what defines a command, i.e. "stats" */
    public abstract String getKey();

    /** Description of the command, how to use and what it does */
    private String description;

    /** Params of the command, as a list of strings, i.e. "!br stats curlicue" where "curlicue" is param number 0 */
    private ArrayList<String> params = new ArrayList<>();

    /** Each Command object will yield a discord message, this is the ID of that message, null before it's sent */
    private String discordMessageId;

    /**
     * Each Command object will yield a discord message, this is the id of the channel where it's sent,
     * null before it's sent
     */
    private String discordChannelId;

    /** Method used to return the reply message, must be implemented */
    public abstract EmbedBuilder getReply();

    /** instance of our CommandEventsListener */
    public CommandEventsListener eventsListener;

    /**
     * set our CommandEventsListener instance
     *
     * @param eventsListener the CommandEventsListener instance
     */
    public void setOnCommandEventsListener(CommandEventsListener eventsListener) {
        this.eventsListener = eventsListener;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the params
     */
    public ArrayList<String> getParams() {
        return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(ArrayList<String> params) {
        this.params = params;
    }

    /**
     * @return the discordMessageId
     */
    public String getDiscordMessageId() {
        return discordMessageId;
    }

    /**
     * @param discordMessageId the params to set
     */
    public void setDiscordMessageId(String discordMessageId) {
        this.discordMessageId = discordMessageId;
    }

}

