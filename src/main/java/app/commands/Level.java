package app.commands;

import com.google.gson.Gson;

import net.dv8tion.jda.core.EmbedBuilder;

import app.commands.core.Command;
import app.db.DbRequests;
import app.db.models.Player;
import app.rest.HttpRequests;
import app.rest.pojos.PlayerPOJO;
import app.utils.BattleriteUtils;
import app.utils.GenericUtils;
import app.utils.TextUtils;
import java.util.Arrays;
import java.util.List;
import retrofit2.Response;

public class Level extends Command {

    public final static String KEY = "rank";

    private String championName;
    private String playerName;

    private PlayerPOJO.Data playerData;

    public Level() {
        setDescription(
                String.format("`%1$s %2$s championName` - Display a persons virtual level on a champion.",
                        GenericUtils.COMMAND_TRIGGER, getKey()));
    }
    
    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public EmbedBuilder getReply() {

        try {
            String playerName = getParams().get(0);
            // fetches the player ID from the player name
            Response<PlayerPOJO> playerResponse = HttpRequests.getPlayerByName(playerName);
            GenericUtils.log(new Gson().toJson(playerResponse.body()));

            // checks if player exists
            if (playerResponse.body().getData().isEmpty()) {
                return GenericUtils.getBasicEmbedMessage(GenericUtils.ERROR_TITLE, "That's not a player I'm afraid");
            }
            // return error message if something went wrong
            if (playerResponse.isSuccessful())
                return GenericUtils.getBasicEmbedMessage(GenericUtils.ERROR_TITLE,
                        "Found the player but couldn't find any data");

            else if (!playerResponse.isSuccessful())
                return GenericUtils.getBasicEmbedMessage(GenericUtils.ERROR_TITLE, GenericUtils.ERROR_MESSAGE);

            playerData = playerResponse.body().getData().get(0);
            DbRequests.savePlayer(playerData);

        } catch (Exception e) {
            e.printStackTrace();
            return GenericUtils.getBasicEmbedMessage(GenericUtils.ERROR_TITLE, GenericUtils.ERROR_MESSAGE);
        }
        return virtualLevel();
    }

    private void init() {
        championName = TextUtils.capitalizeString(getParams().get(0));

        // check if it's ruh kaan
        if (championName.equalsIgnoreCase("ruh")) {
            championName = championName + " " + TextUtils.capitalizeString(getParams().get(1));
        }

        try {
            if (championName.toLowerCase().contains("ruh"))
                playerName = getParams().get(2);
            else
                playerName = getParams().get(1);
        } catch (IndexOutOfBoundsException ignored) {
        }

    }
    
        public EmbedBuilder yes() {

        init();

        if (!isChampion()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(championName).append(" is not a champion, the options are:\n\n");
            for (String champion : BattleriteUtils.CHAMPIONS)
                stringBuilder.append(champion).append("\n");
            return GenericUtils.getBasicEmbedMessage(GenericUtils.ERROR_TITLE, stringBuilder.toString());
        }

        // get the passed player from db
        Player player = DbRequests.getSinglePlayerStats(championName, playerName);
        

            Player playerUpdatedFromDb = null;
            try {
                // get the updated passed player from API
                PlayerPOJO.Data playerUpdated = HttpRequests.getPlayerByName(playerName).body().getData().get(0);
                // save the player in the db
                DbRequests.savePlayer(playerUpdated);
                // get the player again from the db to get his position
                playerUpdatedFromDb = DbRequests.getSinglePlayerStats(championName, playerName);
            } catch (Exception e) {
                GenericUtils.log("couldn't get player " + playerName + " from API");
                e.printStackTrace();
            }
            
            
        EmbedBuilder testt = null;
    
        
        return testt;
    }
        
    private EmbedBuilder virtualLevel() {
        return null;
    }
    
        private EmbedBuilder testt() {
        return null;
    }

    /**
     * Returns true if it's a champion
     */
    private boolean isChampion() {
        return Arrays.asList(BattleriteUtils.CHAMPIONS).contains(championName);
    }

    private void calculateTableSpace(List<PlayerPOJO.Data> players) {
        for (PlayerPOJO.Data p : players) {
            String position = p.getPosition() != null ? p.getPosition() : String.valueOf(players.indexOf(p) + 1);
            String name = p.getAttributes().getName();
            int wins = p.getChampionWins(championName);
            int losses = p.getChampionLosses(championName);
            int totalWin = wins + losses;
            int totalExp = totalWin * 222;
            int virtualLevel = ((totalExp - 69354) / 8000) + 20; 
            int virtualPrestige = (totalWin*222)/69345;
            
            //System.out.println(String virtualLevel);
            //System.out.println(String virtualPrestige);
            
        }

    }}
