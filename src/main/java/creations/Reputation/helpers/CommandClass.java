package creations.Reputation.helpers;

import creations.Reputation.main;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class CommandClass implements CommandExecutor, TabCompleter {
    main plugin = main.getPlugin(main.class);
    EventListener eventListener = new EventListener();
    DatabaseManager databaseManager =  new DatabaseManager();
    boolean check = new ConfigClass().checkConfig();

    private static final String[] COMMANDS = {"menu", "notifications"};

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (command.getName().equalsIgnoreCase("reputation") ) {
            if(args.length >= 1) {
                if (Objects.equals(args[0], "reload")) {
                    if(sender.isOp()) {
                        plugin.reloadPlugin();
                        plugin.getLogger().log(Level.INFO, "Plugin has been reloaded ");
                        if (isPlayer(sender)) sender.sendMessage("Plugin has been reloaded");
                    }
                }
                if(isPlayer(sender)) {
                    Player player = (Player) sender;
                    if (check || !databaseManager.Disabled) {
                        if (Objects.equals(args[0], "menu")) {
                            if (args.length == 2) {
                                if (databaseManager.playerExistsName(args[1])) {
                                    eventListener.CreateMainGUI(player, args[1]);
                                } else {
                                    player.sendMessage(plugin.colorize("&e&l" + args[1] + "&e is not a valid player"));
                                }
                            } else {
                                eventListener.CreateMainGUI(player, player.getName());
                            }
                        } else if (Objects.equals(args[0], "notifications")) {
                            databaseManager.UpdateNotificationsEnabled(player.getName());
                        } else if (Objects.equals(args[0], "delete")){
                            if(player.isOp()) {
                                if (args.length == 2) {
                                    try {
                                        databaseManager.deleteREP(Integer.parseInt(args[1]), player.getName());
                                        player.sendMessage(plugin.colorize("&e&l" + args[1] + "&e has been deleted"));

                                    } catch (NumberFormatException nfe) {
                                        player.sendMessage(plugin.colorize("&e&l" + args[1] + "&e is not a valid number"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean isPlayer(CommandSender sender){
        if(sender instanceof Player) {
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();
        StringUtil.copyPartialMatches(args[0], Arrays.asList(COMMANDS), completions);
        return completions;
    }
}
