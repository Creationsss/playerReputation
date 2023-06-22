package creations.Reputation.helpers;

import creations.Reputation.main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;

public class DatabaseManager {
    main plugin = main.getPlugin(main.class);

    private Connection connection;
    private String host;
    private String database;
    private int port;
    private String username;
    private String password;
    private String table;

    //static local
    private static String playerStatic;
    private static String commandStatic;
    private static Player CommandSenderStatic;
    private static String reasonStatic;
    private static UUID playerUUIDStatic;;



    public static HashMap<String, Long> giveRepCoolDowns = new HashMap<>();
    boolean check = new ConfigClass().checkConfig();

    private void setSQLValues(){
        ConfigClass configClass = new ConfigClass();
        List<String> values = configClass.GetSQLValues();

        host = values.get(0);
        port = Integer.parseInt(values.get(1));
        database = values.get(2);
        username = values.get(3);
        password = values.get(4);
        table = values.get(5);
    }

    public void startSQLConnection(){
        setSQLValues();
        startSQLDriver();
        createSQLTables();
    }

    public Connection getConnectionSql() {
        if (connection == null) {
            startSQLConnection();
        }

        return connection;
    }

    private void startSQLDriver() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String connectionString = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true";
            connection = DriverManager.getConnection(connectionString, username, password);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load MySQL driver.", e);
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Couldn't connect to the given SQL database please fix and reload");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public void createSQLTables() {
        try {
            PreparedStatement mainTable = this.getConnectionSql().prepareStatement("CREATE TABLE IF NOT EXISTS " + this.table + " (UUID TEXT, Name TEXT, PosRep INT, NegRep INT, RepIDLists TEXT, repSENT TEXT, getNotifications BIT)");
            mainTable.executeUpdate();

            PreparedStatement reasonsTable = this.getConnectionSql().prepareStatement("CREATE TABLE IF NOT EXISTS " + this.table + "_reasons" +  " (RepID INT, giverUUID TEXT, giverName TEXT, playerNAME TEXT, PlayerUUID TEXT, reason TEXT, Rep TEXT)");
            reasonsTable.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //start of player functions
    public boolean playerExistsUUID(String uuid) {
        try {
            PreparedStatement statement = this.getConnectionSql().prepareStatement("SELECT COUNT(*) FROM " + this.table + " WHERE UUID=?");
            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                int count = results.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean playerExistsName(String name) {
        try {
            PreparedStatement statement = this.getConnectionSql().prepareStatement("SELECT COUNT(*) FROM " + this.table + " WHERE Name=?");
            statement.setString(1, name);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                int count = results.getInt(1);
                return count > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void SetStaticCurrentInfo(String player, String command, Player CommandSender, String reason, UUID playerUUID){
        if (player != null) playerStatic = player;
        if(command != null) commandStatic = command;
        if(CommandSender != null ) CommandSenderStatic = CommandSender;
        if(reason != null) reasonStatic = reason;
        if(playerUUID != null) playerUUIDStatic = playerUUID;
    }

    public List<Object> ReturnStaticCurrentInfo(){
        return Arrays.asList(playerStatic, commandStatic, CommandSenderStatic, reasonStatic, playerUUIDStatic);
    }

    public void createPlayer(UUID uuid, Player player) {
        String UUID = uuid.toString();
        if (!playerExistsUUID(UUID)) {
            try {
                PreparedStatement statement = this.getConnectionSql().prepareStatement("INSERT INTO " + this.table + " (UUID, Name, PosRep, NegRep, RepIDLists, repSENT, getNotifications) VALUES (?,?,?,?,?,?,?)");
                statement.setString(1, UUID);
                statement.setString(2, player.getName());
                statement.setInt(3, 0);
                statement.setInt(4, 0);
                statement.setString(5, "");
                statement.setString(6, "");
                statement.setBoolean(7, true);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public UUID getUUIDFromName(String name) {
        try {
            PreparedStatement statement = getConnectionSql().prepareStatement("SELECT * FROM " + table + " WHERE Name=?");
            statement.setString(1, name);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                String uuid = results.getString("UUID");
                return UUID.fromString(uuid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String createRepID(String CommandSenderUUID, String CommandSenderNAME, String reason, String RepSent, UUID playerUUID, String PlayerName) {
        try {
            Connection connection = this.getConnectionSql();

            int id;
            boolean isUnique;
            PreparedStatement statement;
            ResultSet results;

            do {
                Random ran = new Random();
                id = ran.nextInt();
                id = Math.abs(id);

                statement = connection.prepareStatement("SELECT * FROM " + this.table + "_reasons" + " WHERE RepID=?");
                statement.setInt(1, id);
                results = statement.executeQuery();

                isUnique = !results.next();
            } while (!isUnique);

            statement = connection.prepareStatement("INSERT INTO " + this.table + "_reasons" + " (RepID, giverUUID, giverName, playerNAME, PlayerUUID, reason, Rep) VALUES (?,?,?,?,?,?,?)");
            statement.setInt(1, id);
            statement.setString(2, CommandSenderUUID);
            statement.setString(3, CommandSenderNAME);
            statement.setString(4, PlayerName);
            statement.setString(5, playerUUID.toString());
            statement.setString(6, reason);
            statement.setString(7, RepSent);
            statement.executeUpdate();


            return Integer.toString(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    Boolean checkNotificationsEnabled(String name){
        try {
            Connection connection = getConnectionSql();
            PreparedStatement PlayerStatement = connection.prepareStatement("SELECT * FROM " + this.table + " WHERE Name=?");
            PlayerStatement.setString(1, name);
            ResultSet PlayersResults = PlayerStatement.executeQuery();

            if(PlayersResults.next()){
                return PlayersResults.getBoolean("getNotifications");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void UpdateNotificationsEnabled(String name){
        try {
            Connection connection = getConnectionSql();
            PreparedStatement PlayerStatement = connection.prepareStatement("SELECT * FROM " + this.table + " WHERE Name=?");
            PreparedStatement update  = connection.prepareStatement("UPDATE " + this.table + " SET getNotifications=? WHERE Name=?");

            PlayerStatement.setString(1, name);
            ResultSet PlayersResults = PlayerStatement.executeQuery();

            if(PlayersResults.next()){
                boolean enabled = PlayersResults.getBoolean("getNotifications");

                update.setBoolean(1, !enabled);
                update.setString(2, name);
                update.executeUpdate();
                Player player = Bukkit.getPlayer(getUUIDFromName(name));

                if(!enabled){
                    player.sendMessage(plugin.colorize("&eYou will now get reputation notifications"));
                }else{
                    player.sendMessage(plugin.colorize("&eYou will no longer get reputation notifications"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    boolean CanRepPlayer(String name, Player sender) {
        ArrayList<ArrayList<String>> allSent = GetAllSentReputationPlayer(sender.getName());

        for (int i = 0; i < allSent.size(); i++) {
            if (allSent.get(i).get(3).equals(name)) {
                return false;
            }
        }
        return true;
    }

    void updateREP(String player, String command, Player CommandSender, String reason, UUID playerUUID) {
        String perm = new ConfigClass().returnOthers().get(1);
        if (!CommandSender.hasPermission(perm)){
            giveRepCoolDowns.put(CommandSender.getName(), System.currentTimeMillis());
        }

        try {
            Connection connection = getConnectionSql();
            PreparedStatement PlayerStatement = connection.prepareStatement("SELECT * FROM " + this.table + " WHERE Name=?");
            PreparedStatement statement = connection.prepareStatement("UPDATE " + this.table + " SET PosRep=?, NegRep=?, RepIDLists=? WHERE Name=?");
            PreparedStatement UpdateStatement = connection.prepareStatement("UPDATE " + this.table + " SET repSENT=? WHERE Name=?");

            PlayerStatement.setString(1, player);
            ResultSet PlayersResults = PlayerStatement.executeQuery();


            if (PlayersResults.next()) {
                int PositiveReputation = PlayersResults.getInt("PosRep");
                int NegativeReputation = PlayersResults.getInt("NegRep");

                statement.setString(4, player);

                String repSent = null;
                if (Objects.equals(command, "Positively")) {
                    PositiveReputation  = PositiveReputation + 1;
                    statement.setInt(1, PositiveReputation);
                    statement.setInt(2, NegativeReputation);
                    repSent = "Positive";
                } else if (Objects.equals(command, "Negatively")) {
                    NegativeReputation  = NegativeReputation + 1;
                    statement.setInt(1, PositiveReputation);
                    statement.setInt(2, NegativeReputation);
                    repSent = "Negative";
                }

                String repIDList = PlayersResults.getString("RepIDLists");
                String newRepID = createRepID(CommandSender.getUniqueId().toString(), CommandSender.getName(), reason, repSent, playerUUID, player);

                if (Objects.equals(repIDList, "")) {
                    statement.setString(3, newRepID);
                } else {
                    statement.setString(3, repIDList + " " + newRepID);
                }

                statement.executeUpdate();

                PlayerStatement.setString(1, CommandSender.getName());
                ResultSet results = PlayerStatement.executeQuery();

                if(results.next()) {
                    String repSentList = results.getString("repSent");

                    UpdateStatement.setString(2, CommandSender.getName());

                    if (Objects.equals(repSentList, "")) {
                        UpdateStatement.setString(1, newRepID);
                    } else {
                        UpdateStatement.setString(1, repSentList + " " + newRepID);
                    }
                    UpdateStatement.executeUpdate();


                    if (checkNotificationsEnabled(player)) {
                        UUID uuid = getUUIDFromName(player);
                        Player online = Bukkit.getPlayer(uuid);
                        if(online != null) {
                            if (online.isOnline()) {
                                repSent = repSent.equals("Positive") ? "Positively" : "Negatively";

                                String overview = plugin.colorize("&eReputation&r:");
                                String updated = plugin.colorize("  &eYour reputation just got updated");
                                String info = plugin.colorize("  &eFrom &6&n" + CommandSender.getName() + "&r, &6&l" + repSent + "&r");
                                String reasonBack = plugin.colorize("  &eReason&r: &6&l&n" + reason);
                                String remaining = plugin.colorize("  &eCurrent reputation&r: &6&l" + PositiveReputation + "&e Positive &6&l" + NegativeReputation + " &eNegative ");
                                online.sendMessage(overview, updated, info, reasonBack, remaining);
                            }
                        }
                    }
                }
            } else {
                CommandSender.sendMessage("That player does not exist");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    void deleteREP(int id, String sender) {
        try{
            Connection connection = this.getConnectionSql();
            PreparedStatement deleteTable = connection.prepareStatement("SELECT * FROM " + this.table + "_reasons" + " WHERE RepID=?");
            PreparedStatement deleteStatement = connection.prepareStatement("DELETE FROM " + this.table + "_reasons" + " WHERE RepID=?");
            PreparedStatement originalTable = connection.prepareStatement("SELECT * FROM " + this.table + " WHERE Name=?");
            PreparedStatement originalTableUpdate = connection.prepareStatement("UPDATE " + this.table + " SET RepIDLists=?, PosREp=?, NegRep=? WHERE Name=?");
            PreparedStatement originalTableSenderUpdate = connection.prepareStatement("UPDATE " + this.table + " SET repSENT=? WHERE Name=?");

            List<String> idInfo = GetReputationIDInfo(Integer.toString(id));
            String name = idInfo.get(4);

            deleteTable.setInt(1, id);
            ResultSet deleteTableResults = deleteTable.executeQuery();

            if (deleteTableResults.next()) {
                deleteStatement.setInt(1, id);
                deleteStatement.executeUpdate();

                String repPolarity = deleteTableResults.getString("Rep");

                originalTable.setString(1, name);
                ResultSet originalTableResults = originalTable.executeQuery();

                if(originalTableResults.next()){
                    String idList = originalTableResults.getString("RepIDLists");
                    String idString = Integer.toString(id);
                    int PositiveRep = originalTableResults.getInt("PosRep");
                    int NegativeRep = originalTableResults.getInt("NegRep");
                    if (idList.contains(idString)) {
                        idList = idList.replace(idString, "");

                        originalTableUpdate.setString(4, name);
                        originalTableUpdate.setString(1, idList);

                        if (Objects.equals(repPolarity, "Positive")) {
                            originalTableUpdate.setInt(2, PositiveRep - 1);
                            originalTableUpdate.setInt(3, NegativeRep);
                        } else if (Objects.equals(repPolarity, "Negative")) {
                            originalTableUpdate.setInt(2, PositiveRep);
                            originalTableUpdate.setInt(3, NegativeRep - 1);
                        }

                        originalTableUpdate.executeUpdate();
                    }

                    originalTable.setString(1, sender);
                    originalTableResults = originalTable.executeQuery();

                    if(originalTableResults.next()){
                        idList = originalTableResults.getString("repSENT");
                        if (idList.contains(idString)) {
                            String UpdatedIdList = idList.replace(idString, "");

                            originalTableSenderUpdate.setString(1, UpdatedIdList);
                            originalTableSenderUpdate.setString(2, sender);
                            originalTableSenderUpdate.executeUpdate();
                        }
                    }

                }
            }else {
                Player player = Bukkit.getPlayer(getUUIDFromName(sender));
                player.sendMessage(plugin.colorize("&eThat is not a existing ID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    List<String> GetReputationIDInfo(String id){
        try {
            Connection connection = this.getConnectionSql();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + this.table + "_reasons" + " WHERE RepID=?");
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();

            if (results.next()) {
                String giverUUID = results.getString("giverUUID");
                String playerName = results.getString("playerNAME");
                String giverNAME = results.getString("giverName");
                String reason = results.getString("reason");
                String polarity = results.getString("Rep");

                return Arrays.asList(giverUUID, giverNAME, reason, polarity, playerName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    public ArrayList<ArrayList<String>> GetAllSentReputationPlayer(String name) {
        try {
            Connection connection = getConnectionSql();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + this.table + " WHERE Name=?");
            PreparedStatement reasonsStatement = connection.prepareStatement("SELECT * FROM " + this.table + "_reasons");

            statement.setString(1, name);
            ResultSet results = statement.executeQuery();
            if(results.next()) {

                List<String> idLists;
                String RepSent = results.getString("repSENT");
                idLists = Arrays.asList(RepSent.split(" "));

                ResultSet reasonsResults = reasonsStatement.executeQuery();
                ArrayList<ArrayList<String>> finalList = new ArrayList<>();

                while (reasonsResults.next()) {
                    ArrayList<String> row = new ArrayList<>();
                    for (int i = 1; i <= 7; i++) {
                        row.add(reasonsResults.getString(i));
                    }

                    if (idLists.contains(row.get(0))) {
                        finalList.add(row);
                    }
                }

                return finalList;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }



    List<String> GetAllReputationPlayerInfo(String player) {
        try {
            Connection connection = this.getConnectionSql();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + this.table + " WHERE Name=?");
            statement.setString(1, player);
            ResultSet results = statement.executeQuery();

            if (results.next()) {
                int PositiveRep = results.getInt("PosRep");
                int NegativeRep = results.getInt("NegRep");
                String repGiven = results.getString("repSENT");
                int overall = PositiveRep - NegativeRep;
                String standing;

                if (overall > 0) {
                    standing = "In good standing";
                } else if (overall < 0) {
                    standing = "In bad standing";
                } else {
                    standing = "In neutral standing";
                }

                return Arrays.asList(Integer.toString(PositiveRep), Integer.toString(NegativeRep), Integer.toString(overall), standing, repGiven);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    ArrayList<ArrayList<String>> GetAllReputationPlayer(String player) {
        try {
            Connection connection = this.getConnectionSql();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + this.table + "_reasons WHERE playerNAME=?");
            statement.setString(1, player);
            ResultSet results = statement.executeQuery();
            ArrayList<ArrayList<String>> finalList = new ArrayList<>();

            while (results.next()) {
                ArrayList<String> row = new ArrayList<>();
                for (int i = 1; i <= 7; i++) {
                    row.add(results.getString(i));
                }
                finalList.add(row);
            }

            return finalList;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    ArrayList<ArrayList<String>> GetAllReputationPlayersInfo() {
        ArrayList<ArrayList<String>> wholeLIST1 = new ArrayList<>();

        try {
            Connection connection = this.getConnectionSql();
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + this.table);
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                String uuid = results.getString("UUID");
                String name = results.getString("Name");
                int PositiveRep = results.getInt("PosRep");
                int NegativeRep = results.getInt("NegRep");
                int overall = PositiveRep - NegativeRep;
                String standing;

                if (overall > 0) {
                    standing = "In good standing";
                } else if (overall < 0) {
                    standing = "In bad standing";
                } else {
                    standing = "In neutral standing";
                }
                ArrayList<String> wholeLIST = new ArrayList<>();

                wholeLIST.add(uuid);
                wholeLIST.add(name);
                wholeLIST.add(Integer.toString(PositiveRep));
                wholeLIST.add(Integer.toString(NegativeRep));
                wholeLIST.add(Integer.toString(overall));
                wholeLIST.add(standing);
                wholeLIST1.add(wholeLIST);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return wholeLIST1;
    }
}