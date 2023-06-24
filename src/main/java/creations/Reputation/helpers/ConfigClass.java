package creations.Reputation.helpers;

import creations.Reputation.main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class ConfigClass {
    main plugin = main.getPlugin(main.class);
    DatabaseManager databaseManager = new DatabaseManager();

    private File customConfigFile;
    private FileConfiguration customConfig;

    public static String reputationMenuName;
    public static String playerReputation;
    public static String sentReputation;
    public static String allReputation;
    public static String confirmationMenuName;

    public static Material givenItem;
    public static Material sentItem;
    public static Material serverSentItem;
    public static Material backButton;
    public static Material nextButton;
    public static Material positiveButton;
    public static Material negativeButton;
    public static Material blockedButton;

    public static String givenItemText;
    public static String sentItemText;
    public static String serverSentItemText;
    public static String positiveButtonText;
    public static String negativeButtonText;
    public static String blockedButtonText;
    public static String timerMessage;
    public static String mainGUIHeadMessage;
    public static String playersGUIHeadMessage;
    public static String playersGivenGUIHeadMessage;
    public static String backButtonText;


    public void CheckConfig() {
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveDefaultConfig();
        createMessageConfig();
        SetCustomMessages();

        if(!checkConfig()){
            new DatabaseManager().Disabled = true;
            plugin.getLogger().log(Level.SEVERE, "Please setup the SQL part of the configuration file!");
        }
    }

    public void SetCustomMessages(){
        reputationMenuName = main.colorize(getMessageConfig().getString("reputation-menu-name"));
        playerReputation = main.colorize(getMessageConfig().getString("player-reputation-name"));
        sentReputation = main.colorize(getMessageConfig().getString("sent-reputation-name"));
        allReputation = main.colorize(getMessageConfig().getString("all-reputation-name"));
        confirmationMenuName = main.colorize(getMessageConfig().getString("confirmation-menu-name"));

        givenItem = Material.getMaterial(getMessageConfig().getString("given-to-player").toUpperCase());
        sentItem = Material.getMaterial(getMessageConfig().getString("sent-by-player").toUpperCase());
        serverSentItem = Material.getMaterial((getMessageConfig().getString("server-rep-list").toUpperCase()));
        backButton = Material.getMaterial((getMessageConfig().getString("back-button").toUpperCase()));
        nextButton = Material.getMaterial((getMessageConfig().getString("nextOrPrevious-button").toUpperCase()));
        positiveButton = Material.getMaterial((getMessageConfig().getString("positive-button").toUpperCase()));
        negativeButton = Material.getMaterial((getMessageConfig().getString("negative-button").toUpperCase()));
        blockedButton = Material.getMaterial((getMessageConfig().getString("unable-toRep").toUpperCase()));


        //actual lore, messages and responses

        givenItemText = main.colorize(getMessageConfig().getString("given-to-player-text"));
        sentItemText = main.colorize(getMessageConfig().getString("sent-by-player-text"));
        serverSentItemText = main.colorize(getMessageConfig().getString("server-rep-list-text"));
        positiveButtonText = main.colorize(getMessageConfig().getString("positive-button-text"));
        negativeButtonText = main.colorize(getMessageConfig().getString("negative-button-text"));
        blockedButtonText = main.colorize(getMessageConfig().getString("unable-toRep-text"));
        timerMessage = main.colorize(getMessageConfig().getString("repTimer-message"));
        mainGUIHeadMessage = main.colorize(getMessageConfig().getString("playerDisplayHead-message"));
        playersGUIHeadMessage = main.colorize(getMessageConfig().getString("server-rep-list-playerDisplayHead-message"));
        playersGivenGUIHeadMessage = main.colorize(getMessageConfig().getString("player-reputation-playerDisplayHead-message"));
        backButtonText = main.colorize(getMessageConfig().getString("back-button-text"));
    }

    public FileConfiguration getMessageConfig() {
        return customConfig;
    }

    public void createMessageConfig() {
        customConfigFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            plugin.saveResource("messages.yml", false);
        }

        customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public List<String> GetSQLValues() {
        String host = plugin.getConfig().getString("host");
        int port = plugin.getConfig().getInt("port");
        String database = plugin.getConfig().getString("database");
        String username = plugin.getConfig().getString("username");
        String password = plugin.getConfig().getString("password");
        String table = plugin.getConfig().getString("table");

        return Arrays.asList(host, Integer.toString(port), database, username, password, table);
    }

    public List<String> returnOthers(){
        int coolDown = plugin.getConfig().getInt("RepCooldown");
        String ignoreCD = plugin.getConfig().getString("IgnoreCoolDown");
        return Arrays.asList(Integer.toString(coolDown), ignoreCD);
    }

    public boolean checkConfig() {
        List<String> values =  GetSQLValues();
        if (!values.get(0).equals("host") && Integer.parseInt(values.get(1)) != 0 && !values.get(2).equals("database") && !values.get(3).equals("username") && !values.get(4).equals("password") && !values.get(5).equals("table")) {
            return true;
        } else {
            return false;
        }
    }
}
