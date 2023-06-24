package creations.Reputation;

import creations.Reputation.helpers.*;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.Callable;

public class main extends JavaPlugin {


    @Override
    public void onEnable() {
        startCommands();
    }

    @Override
    public void onDisable() {
        DatabaseManager.Disabled = true;
    }

    public void addAPIS(){
        int pluginId = 18863;
        Metrics metrics = new Metrics(this, pluginId);

        metrics.addCustomChart(new SingleLineChart("servers", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 1;
            }
        }));

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            new PlaceholderAPIClass().register();
        }
    }

    public void reloadPlugin(){
        DatabaseManager.Disabled = false;
        ConfigClass configClass = new ConfigClass();

        this.reloadConfig();
        configClass.CheckConfig();
        if(configClass.checkConfig()) {
            ReturnSQLDatabase().startSQLConnection();
        }
        configClass.createMessageConfig();
        configClass.SetCustomMessages();
    }

    public void startCommands(){
        ConfigClass configClass = new ConfigClass();
        this.getCommand("reputation").setExecutor(new CommandClass());
        this.getServer().getPluginManager().registerEvents(new EventListener(), this);
        configClass.CheckConfig();
        if(configClass.checkConfig()) {
            if(!DatabaseManager.Disabled) {
                ReturnSQLDatabase().startSQLConnection();
            }
        }
        addAPIS();
    }

    public static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String replaceValues(String message,String Values,  List<String> list){
        switch (Values) {
            case "name" -> message = message.replace("[name]", list.get(0));
            case "time" -> {
                message = message.replace("[hours]", list.get(0));
                message = message.replace("[minutes]", list.get(1));
                message = message.replace("[seconds]", list.get(2));
            }
            case "MainGUIHeadMessage", "playersGUIHeadMessage" -> {
                message = message.replace("[positive]", list.get(0));
                message = message.replace("[negative]", list.get(1));
                message = message.replace("[combined]", list.get(2));
                message = message.replace("[standing]", list.get(3));
            }
            case "playerREP" ->{
                message = message.replace("[polarity]", list.get(0));
                message = message.replace("[repid]", list.get(1));
                message = message.replace("[reason]", list.get(2));
            }
        }
        return message;
    }

    public DatabaseManager ReturnSQLDatabase(){
        return new DatabaseManager();
    }

}
