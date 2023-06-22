package creations.Reputation.helpers;

import creations.Reputation.main;
import org.bukkit.Bukkit;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class ConfigClass {
    main plugin = main.getPlugin(main.class);

    public void CheckConfig() {
        plugin.getConfig().options().copyDefaults(true);
        plugin.saveDefaultConfig();

        if(!checkConfig()){
            plugin.getLogger().log(Level.SEVERE, "Please setup the SQL part of the configuration file!");
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
