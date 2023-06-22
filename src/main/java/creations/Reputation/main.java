package creations.Reputation;

import creations.Reputation.helpers.CommandClass;
import creations.Reputation.helpers.ConfigClass;
import creations.Reputation.helpers.DatabaseManager;
import creations.Reputation.helpers.EventListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin {
    @Override
    public void onEnable() {
        startCommands();
    }

    @Override
    public void onDisable() {
    }

    public void reloadPlugin(){
        ConfigClass configClass = new ConfigClass();
        configClass.CheckConfig();
        if(configClass.checkConfig()) {
            ReturnSQLDatabase().startSQLConnection();
        }
    }

    public void startCommands(){
        ConfigClass configClass = new ConfigClass();
        this.getCommand("reputation").setExecutor(new CommandClass());
        this.getServer().getPluginManager().registerEvents(new EventListener(), this);
        configClass.CheckConfig();
        if(configClass.checkConfig()) {
            ReturnSQLDatabase().startSQLConnection();
        }
    }

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public DatabaseManager ReturnSQLDatabase(){
        return new DatabaseManager();
    }

}
