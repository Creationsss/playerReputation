package creations.Reputation.helpers;

import creations.Reputation.main;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlaceholderAPIClass extends PlaceholderExpansion {
    main plugin = main.getPlugin(main.class);
    PluginDescriptionFile pdf = plugin.getDescription();
    DatabaseManager databaseManager = new DatabaseManager();


    @Override
    public @NotNull String getIdentifier() {
        return "playerReputation";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Creations";
    }

    @Override
    public @NotNull String getVersion() {
        return pdf.getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if(player == null) return "";
        if(params.startsWith("player")){
            String[] SplitParam = params.split("_");
            if(databaseManager.playerExistsName(SplitParam[1])) {
                String playerName = SplitParam[1];
                List<String> repItems = databaseManager.GetAllReputationPlayerInfo(playerName);
                if (params.endsWith("positive")) {
                    return repItems.get(2);
                } else if (params.endsWith("negative")) {
                    return repItems.get(3);
                }else if (params.endsWith("combined")){
                    return repItems.get(4);
                }else if (params.endsWith("standing")){
                    return repItems.get(5);
                }
            }
        } else if (params.startsWith("repinfo")) {
            String[] SplitParam = params.split("_");
            String repID = SplitParam[1];
            List<String> repItems = databaseManager.GetReputationIDInfo(repID);
            if (params.endsWith("giveruuid")){
                return repItems.get(0);
            }else if (params.endsWith("playername")){
                return repItems.get(4);
            }else if (params.endsWith("givername")){
                return repItems.get(1);
            }else if (params.endsWith("reason")){
                return repItems.get(2);
            }else if (params.endsWith("polarity")){
                return repItems.get(3);
            }
        } else if (params.startsWith("topplayer")) {
            List<String> repItems = databaseManager.topPlayer();

            if (params.endsWith("positive")) {
                return repItems.get(0);
            } else if (params.endsWith("negative")) {
                return repItems.get(1);
            }else if (params.endsWith("combined")){
                return repItems.get(2);
            }else if (params.endsWith("standing")){
                return repItems.get(3);
            }else if (params.endsWith("uuid")){
                return repItems.get(4);
            }else if (params.endsWith("name")){
                return repItems.get(5);
            }
        }else if (params.startsWith("repcount")) {
            int count = 0;
            if (params.endsWith("positive")) {
                count = databaseManager.getSentCount("positive");
            }else if (params.endsWith("negative")) {
                count = databaseManager.getSentCount("negative");
            }else if (params.endsWith("all")) {
                count = databaseManager.getSentCount("all");
            }
            return Integer.toString(count);
        }
        return null;
    }
}
