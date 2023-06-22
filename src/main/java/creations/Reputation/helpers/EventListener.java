package creations.Reputation.helpers;

import creations.Reputation.main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.logging.Level;

public class EventListener implements Listener {
    main plugin = main.getPlugin(main.class);
    DatabaseManager databaseManager = new DatabaseManager();

    static int currentPage = 0;
    static int PageCount = 0;
    static int currentMenu = 0;

    private ConversationFactory conversationFactory;
    private ConversationFactory getConversationFactory() {
        if (conversationFactory == null) {
            conversationFactory = new ConversationFactory(plugin);
        }
        return conversationFactory;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent PJ) {
        Player player = PJ.getPlayer();
        UUID PlayerUUID = player.getUniqueId();
        Boolean bol = new ConfigClass().checkConfig();
        plugin.getLogger().log(Level.INFO, bol.toString());
        databaseManager.createPlayer(PlayerUUID, player);
    }

    @EventHandler
    public void clickevent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory clickedInventory = e.getClickedInventory();
        List<Object> staticItems = databaseManager.ReturnStaticCurrentInfo();

        Player LocalizedSender = (Player) staticItems.get(2);
        String LocalizedPlayer = (String) staticItems.get(0);

        Conversation s = getConversationFactory().withFirstPrompt(new ConversationClass()).thatExcludesNonPlayersWithMessage("Cannot Be used Inside the console").withLocalEcho(false).buildConversation(player);
        if (clickedInventory == null) {
            return;
        }
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null) {
            return;
        }
        if (e.getView().getTitle().equals("Reputation Menu")) {
            switch (clickedItem.getType()) {
                case LIME_WOOL:
                    databaseManager.SetStaticCurrentInfo(null, "Positively", null, null, null);
                    s.begin();
                    e.getView().close();
                    break;
                case RED_WOOL:
                    databaseManager.SetStaticCurrentInfo(null, "Negatively", null, null, null);
                    s.begin();
                    e.getView().close();
                    break;
                case BOOK:
                    allReputationPlayerGUI(LocalizedSender, LocalizedPlayer, 0);
                    break;
                case PAPER:
                    allReputationPlayerSentGUI(LocalizedSender, LocalizedPlayer, 0);
                    break;
                case BOOKSHELF:
                    allPlayerREPSGUI(player,0);
            }
            e.setCancelled(true);
        } else if (e.getView().getTitle().endsWith("reputation")) {
            switch (clickedItem.getType()) {
                case ARROW:
                    String buttonName = clickedItem.getItemMeta().getDisplayName();
                    currentPage += buttonName.startsWith(ChatColor.GREEN + "Next Page") ? 1 : 0;
                    currentPage -= buttonName.startsWith(ChatColor.GREEN + "Previous Page") ? 1 : 0;
                    switch (currentMenu) {
                        case 0-> allReputationPlayerGUI(player, LocalizedPlayer, currentPage);
                        case 1-> allReputationPlayerSentGUI(LocalizedSender, LocalizedPlayer, currentPage);
                    }
                    break;
                    
                case RED_WOOL:
                    CreateMainGUI(LocalizedSender, LocalizedPlayer);
                    break;

                case PLAYER_HEAD:
                    if(e.isRightClick()) {
                        if (player.isOp()) {
                            NamespacedKey key = new NamespacedKey(plugin, "repID");
                            PersistentDataContainer container = clickedItem.getItemMeta().getPersistentDataContainer();
                            String id = container.get(key, PersistentDataType.STRING);
                            deleteUI(player, id);
                        }
                    }else{
                        String dn = clickedItem.getItemMeta().getDisplayName();
                        String playername = dn.substring(dn.lastIndexOf(' ') + 1);
                        CreateMainGUI(LocalizedSender, playername);
                    }
            }
            e.setCancelled(true);
        } else if (e.getView().getTitle().equals("Players")) {
            switch (clickedItem.getType()) {
                case ARROW:
                    String buttonName = clickedItem.getItemMeta().getDisplayName();
                    currentPage += buttonName.startsWith(ChatColor.GREEN + "Next Page") ? 1 : 0;
                    currentPage -= buttonName.startsWith(ChatColor.GREEN + "Previous Page") ? 1 : 0;
                    allReputationPlayerGUI(player, LocalizedPlayer, currentPage);
                    break;
                case RED_WOOL:
                    CreateMainGUI(LocalizedSender, LocalizedPlayer);
                    break;
                case PLAYER_HEAD:
                    CreateMainGUI(LocalizedSender, clickedItem.getItemMeta().getDisplayName());
            }
            e.setCancelled(true);
        }else if (e.getView().getTitle().equals("Confirmation Menu")) {
            switch (clickedItem.getType()) {
                case RED_WOOL -> {
                    switch (currentMenu) {
                        case 0 -> allReputationPlayerGUI(player, LocalizedPlayer, currentPage);
                        case 1 -> allReputationPlayerSentGUI(player, LocalizedPlayer, currentPage);
                    }
                }
                case LIME_WOOL -> {
                    NamespacedKey key = new NamespacedKey(plugin, "repID");
                    PersistentDataContainer container = clickedItem.getItemMeta().getPersistentDataContainer();
                    String id = container.get(key, PersistentDataType.STRING);
                    databaseManager.deleteREP(Integer.parseInt(id), LocalizedSender.getName());
                    switch (currentMenu) {
                        case 0 -> allReputationPlayerGUI(player, LocalizedPlayer, currentPage);
                        case 1 -> allReputationPlayerSentGUI(player, LocalizedPlayer, currentPage);
                    }
                }
            }
            e.setCancelled(true);
        }
    }

    public void deleteUI(Player player, String id){
        Inventory gui = Bukkit.createInventory(player, 9, "Confirmation Menu");

        List<String> currentIDSET = databaseManager.GetReputationIDInfo(id);

        String uuid = currentIDSET.get(0);
        String repSender = currentIDSET.get(1);
        String reason = currentIDSET.get(2);
        String polarity = currentIDSET.get(3);

        ItemStack deleteConfirmation = new ItemStack(Material.LIME_WOOL);
        ItemMeta deleteConfirmationMeta = deleteConfirmation.getItemMeta();
        deleteConfirmationMeta.setDisplayName(ChatColor.GREEN + "Delete");
        NamespacedKey key = new NamespacedKey(plugin, "repID");
        deleteConfirmationMeta.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
        deleteConfirmation.setItemMeta(deleteConfirmationMeta);

        ItemStack cancelDeletion = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelDeletionMeta = cancelDeletion.getItemMeta();
        cancelDeletionMeta.setDisplayName(ChatColor.RED + "Cancel");
        cancelDeletion.setItemMeta(cancelDeletionMeta);

        ItemStack playerskull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) playerskull.getItemMeta();
        meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
        meta.setDisplayName(repSender);

        ArrayList<String> playerLore = new ArrayList<>();
        playerLore.add(polarity);
        playerLore.add("ID: " + id);
        playerLore.add("Reason: " + reason);
        meta.setLore(playerLore);
        playerskull.setItemMeta(meta);

        gui.setItem(0, playerskull);
        gui.setItem(7, deleteConfirmation);
        gui.setItem(8, cancelDeletion);

        player.openInventory(gui);
    }

    public void CreateMainGUI(Player player, String name) {
        List<String> repItems = databaseManager.GetAllReputationPlayerInfo(name);
        Inventory gui = Bukkit.createInventory(player, 9, "Reputation Menu");

        UUID uuid = databaseManager.getUUIDFromName(name);
        databaseManager.SetStaticCurrentInfo(name, null, player, null, uuid);
        List<Object> staticItems = databaseManager.ReturnStaticCurrentInfo();

        Player LocalizedSender = (Player) staticItems.get(2);
        String LocalizedPlayer = (String) staticItems.get(0);


        ItemStack playerSkull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerSkull.getItemMeta();
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        skullMeta.setDisplayName(name);

        ArrayList<String> playerLore = new ArrayList<>();
        playerLore.add("Positive: " + repItems.get(0));
        playerLore.add("Negative: " + repItems.get(1));
        playerLore.add("Combined: " + repItems.get(2));
        skullMeta.setLore(playerLore);
        playerSkull.setItemMeta(skullMeta);

        ItemStack positive = new ItemStack(Material.LIME_WOOL);
        ItemMeta checkmarkMeta = positive.getItemMeta();
        checkmarkMeta.setDisplayName(ChatColor.GREEN + "Positively rep this player");
        positive.setItemMeta(checkmarkMeta);

        ItemStack negative = new ItemStack(Material.RED_WOOL);
        ItemMeta negativeMeta = negative.getItemMeta();
        negativeMeta.setDisplayName(ChatColor.RED + "Negatively rep this player");
        negative.setItemMeta(negativeMeta);

        ItemStack AllReputation = new ItemStack(Material.BOOK);
        ItemMeta AllReputationMeta = AllReputation.getItemMeta();
        if (repItems.isEmpty()) {
            AllReputationMeta.setDisplayName(ChatColor.RED + name + " has no current reputation");
        }else {
            AllReputationMeta.setDisplayName(ChatColor.YELLOW + "Check all reputation points given to " + name);
        }
        AllReputation.setItemMeta(AllReputationMeta);

        ItemStack AllReputationSent = new ItemStack(Material.PAPER);
        ItemMeta AllReputationSentMeta = AllReputationSent.getItemMeta();
        AllReputationSentMeta.setDisplayName(ChatColor.YELLOW + "Check all reputation points sent by " + name);
        AllReputationSent.setItemMeta(AllReputationSentMeta);

        ItemStack AllReputationSERVER = new ItemStack(Material.BOOKSHELF);
        ItemMeta AllReputationSERVERMeta = AllReputationSERVER.getItemMeta();
        AllReputationSERVERMeta.setDisplayName(ChatColor.YELLOW + "Check all current reputation points of the server");
        AllReputationSERVER.setItemMeta(AllReputationSERVERMeta);

        ItemStack cannotREP = new ItemStack(Material.BARRIER);
        ItemMeta cannotREPMeta = cannotREP.getItemMeta();
        cannotREPMeta.setDisplayName(ChatColor.RED + "You cannot rep this player");
        cannotREP.setItemMeta(cannotREPMeta);

        int moveIcons = 3;

        ConfigClass configClass = new ConfigClass();

        int CoolDown = Integer.parseInt(configClass.returnOthers().get(0));
        HashMap<String, Long> cooldowns  = databaseManager.giveRepCoolDowns;

        if(cooldowns.containsKey(player.getName())) {
            long secondsLeft = ((cooldowns.get(player.getName()) / 1000) + CoolDown) - (System.currentTimeMillis() / 1000);
            int secondsLeftINT = Math.toIntExact(secondsLeft);
            int hours = secondsLeftINT / 3600;
            int minutes = (secondsLeftINT % 3600) / 60;
            int seconds = secondsLeftINT % 60;

            ItemStack LampINFO = new ItemStack(Material.BARRIER);
            ItemMeta LampINFOMeta = LampINFO.getItemMeta();
            LampINFOMeta.setDisplayName(ChatColor.RED + "You cannot rep someone for another " + hours + "H " + minutes + "M " + seconds + "S");
            LampINFO.setItemMeta(LampINFOMeta);

            if(!LocalizedPlayer.equals(LocalizedSender.getName())) {
                if(secondsLeft < 0) {
                    if(databaseManager.CanRepPlayer(name, player)) {
                        gui.setItem(7, positive);
                        gui.setItem(8, negative);
                        moveIcons = 0;
                    }
                    else {
                        gui.setItem(1, cannotREP);
                    }
                }else {
                    gui.setItem(1, LampINFO);
                }
            }else {
                gui.setItem(1, LampINFO);
            }
        }else {
            if(!LocalizedPlayer.equals(LocalizedSender.getName())) {
                Boolean db = databaseManager.CanRepPlayer(LocalizedPlayer, LocalizedSender);
                plugin.getLogger().log(Level.INFO, db.toString());
                if(databaseManager.CanRepPlayer(name, player))
                {
                    gui.setItem(7, positive);
                    gui.setItem(8, negative);
                    moveIcons = 0;
                }
                else {
                    gui.setItem(1, cannotREP);
                }
            }
        }
        gui.setItem(0, playerSkull);
        gui.setItem(3 + moveIcons, AllReputation);
        gui.setItem(4 + moveIcons, AllReputationSent);
        gui.setItem(5 + moveIcons, AllReputationSERVER);
        player.openInventory(gui);
    }

    public void allReputationPlayerGUI(Player player, String name, int page) {
        currentMenu = 0;
        List<ArrayList<String>> AllReputationList = databaseManager.GetAllReputationPlayer(name);
        currentPage = page;
        int localPageSTR = currentPage + 1;
        int pageSize = (currentPage > 0) ? 51 : 52;
        PageCount = (int) Math.ceil(AllReputationList.size() / (double) pageSize);

        Inventory gui = Bukkit.createInventory(player, 54, name + " reputation");


        int startIndex = currentPage * pageSize;
        startIndex = Math.max(startIndex, 0);
        int endIndex = Math.min(startIndex + pageSize, AllReputationList.size());

        for (int i = startIndex; i < endIndex; i++) {
            String repID = AllReputationList.get(i).get(0);
            String playerName = AllReputationList.get(i).get(2);
            UUID playerUUID = databaseManager.getUUIDFromName(playerName);
            String reason = AllReputationList.get(i).get(5);
            String polarity = AllReputationList.get(i).get(6);


            ItemStack playerskull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) playerskull.getItemMeta();
            NamespacedKey key = new NamespacedKey(plugin, "repID");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, repID);
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerUUID));
            meta.setDisplayName("Sent by " + playerName);

            ArrayList<String> playerLore = new ArrayList<>();
            playerLore.add(polarity);
            playerLore.add("ID: " + repID);
            playerLore.add("Reason: " + reason);
            meta.setLore(playerLore);
            playerskull.setItemMeta(meta);

            gui.addItem(playerskull);
        }
        if (currentPage > 0) {
            ItemStack previousPageItem = new ItemStack(Material.ARROW);
            ItemMeta previousPageMeta = previousPageItem.getItemMeta();
            previousPageMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
            ArrayList<String> skullLore = new ArrayList<>();
            skullLore.add("Current page " + localPageSTR + " out of " + PageCount);
            previousPageMeta.setLore(skullLore);
            previousPageItem.setItemMeta(previousPageMeta);
            int slot = 51;
            slot = (currentPage == PageCount - 1) ? 52 : slot;
            gui.setItem(slot, previousPageItem);
        }
        if (currentPage < PageCount - 1) {
            ItemStack nextPageItem = new ItemStack(Material.ARROW);
            ItemMeta nextPageMeta = nextPageItem.getItemMeta();
            nextPageMeta.setDisplayName(ChatColor.GREEN + "Next Page");
            ArrayList<String> skullLore = new ArrayList<>();
            skullLore.add("Current page " + localPageSTR + " out of " + PageCount);
            nextPageMeta.setLore(skullLore);

            nextPageItem.setItemMeta(nextPageMeta);
            gui.setItem(52, nextPageItem);
        }
        ItemStack backToMenu = new ItemStack(Material.RED_WOOL);
        ItemMeta backToMenuMeta = backToMenu.getItemMeta();
        backToMenuMeta.setDisplayName(ChatColor.RED + "Back to Reputation Screen");
        backToMenu.setItemMeta(backToMenuMeta);
        gui.setItem(53, backToMenu);
        player.openInventory(gui);
    }

    public void allReputationPlayerSentGUI(Player player, String name, int page) {
        currentMenu = 1;
        List<ArrayList<String>> AllReputationList = databaseManager.GetAllSentReputationPlayer(name);

        currentPage = page;
        int localPageSTR = currentPage + 1;
        int pageSize = (currentPage > 0) ? 51 : 52;
        PageCount = (int) Math.ceil(AllReputationList.size() / (double) pageSize);

        Inventory gui = Bukkit.createInventory(player, 54, name + " sent reputation");


        int startIndex = currentPage * pageSize;
        startIndex = Math.max(startIndex, 0);
        int endIndex = Math.min(startIndex + pageSize, AllReputationList.size());

        for (int i = startIndex; i < endIndex; i++) {
            String repID = AllReputationList.get(i).get(0);
            String SentToName = AllReputationList.get(i).get(3);
            UUID playerUUID = databaseManager.getUUIDFromName(SentToName);
            String reason = AllReputationList.get(i).get(5);
            String polarity = AllReputationList.get(i).get(6);


            ItemStack playerskull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) playerskull.getItemMeta();
            NamespacedKey key = new NamespacedKey(plugin, "repID");
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, repID);
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerUUID));
            meta.setDisplayName("Sent to " + SentToName);

            ArrayList<String> playerLore = new ArrayList<>();
            playerLore.add(polarity);
            playerLore.add("ID: " + repID);
            playerLore.add("Reason: " + reason);
            meta.setLore(playerLore);
            playerskull.setItemMeta(meta);

            gui.addItem(playerskull);
        }
        if (currentPage > 0) {
            ItemStack previousPageItem = new ItemStack(Material.ARROW);
            ItemMeta previousPageMeta = previousPageItem.getItemMeta();
            previousPageMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
            ArrayList<String> skullLore = new ArrayList<>();
            skullLore.add("Current page " + localPageSTR + " out of " + PageCount);
            previousPageMeta.setLore(skullLore);
            previousPageItem.setItemMeta(previousPageMeta);
            int slot = 51;
            slot = (currentPage == PageCount - 1) ? 52 : slot;
            gui.setItem(slot, previousPageItem);
        }
        if (currentPage < PageCount - 1) {
            ItemStack nextPageItem = new ItemStack(Material.ARROW);
            ItemMeta nextPageMeta = nextPageItem.getItemMeta();
            nextPageMeta.setDisplayName(ChatColor.GREEN + "Next Page");
            ArrayList<String> skullLore = new ArrayList<>();
            skullLore.add("Current page " + localPageSTR + " out of " + PageCount);
            nextPageMeta.setLore(skullLore);

            nextPageItem.setItemMeta(nextPageMeta);
            gui.setItem(52, nextPageItem);
        }
        ItemStack backToMenu = new ItemStack(Material.RED_WOOL);
        ItemMeta backToMenuMeta = backToMenu.getItemMeta();
        backToMenuMeta.setDisplayName(ChatColor.RED + "Back to Reputation Screen");
        backToMenu.setItemMeta(backToMenuMeta);
        gui.setItem(53, backToMenu);
        player.openInventory(gui);
    }

    public void allPlayerREPSGUI(Player player, int page) {
        currentPage = page;
        List<ArrayList<String>> allPlayerReps = databaseManager.GetAllReputationPlayersInfo();
        int pageSize = (currentPage > 0) ? 51 : 52;
        PageCount = (int) Math.ceil(allPlayerReps.size() / (double) pageSize);
        int localPageSTR = currentPage + 1;

        PageCount = (int) Math.ceil(allPlayerReps.size() / (double) pageSize);
        pageSize = (currentPage > 0) ? 51 : 52;
        if (currentPage < 0) {
            currentPage = 0;
        }
        if (currentPage > PageCount) {
            currentPage = PageCount - 1;
        }
        int startIndex = currentPage * pageSize;
        startIndex = Math.max(startIndex, 0);
        int endIndex = Math.min(startIndex + pageSize, allPlayerReps.size());
        Inventory gui = Bukkit.createInventory(player, 54, "Players");
        for (int i = startIndex; i < endIndex; i++) {
            String uuid = allPlayerReps.get(i).get(0);
            String currname = allPlayerReps.get(i).get(1);
            String posrep = allPlayerReps.get(i).get(2);
            String negrep = allPlayerReps.get(i).get(3);
            String overall = allPlayerReps.get(i).get(4);
            String standing = allPlayerReps.get(i).get(5);
            ItemStack playerskull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) playerskull.getItemMeta();
            meta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(uuid)));
            meta.setDisplayName(currname);
            ArrayList<String> playerLore = new ArrayList<>();
            playerLore.add("Positive: " + posrep);
            playerLore.add("Negative: " + negrep);
            playerLore.add("Combined: " + overall);
            playerLore.add("Standing: " + standing);
            meta.setLore(playerLore);
            playerskull.setItemMeta(meta);
            gui.addItem(playerskull);
        }
        if (currentPage > 0) {
            ItemStack previousPageItem = new ItemStack(Material.ARROW);
            ItemMeta previousPageMeta = previousPageItem.getItemMeta();
            previousPageMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
            ArrayList<String> skullLore = new ArrayList<>();
            skullLore.add("Current page " + localPageSTR + " out of " + PageCount);
            previousPageMeta.setLore(skullLore);
            previousPageItem.setItemMeta(previousPageMeta);
            int slot = 51;
            slot = (currentPage == PageCount - 1) ? 52 : slot;
            gui.setItem(slot, previousPageItem);
        }
        if (currentPage < PageCount - 1) {
            ItemStack nextPageItem = new ItemStack(Material.ARROW);
            ItemMeta nextPageMeta = nextPageItem.getItemMeta();
            nextPageMeta.setDisplayName(ChatColor.GREEN + "Next Page");
            ArrayList<String> skullLore = new ArrayList<>();
            skullLore.add("Current page " + localPageSTR + " out of " + PageCount);
            nextPageMeta.setLore(skullLore);

            nextPageItem.setItemMeta(nextPageMeta);
            gui.setItem(52, nextPageItem);
        }
        ItemStack backToMenu = new ItemStack(Material.RED_WOOL);
        ItemMeta backToMenuMeta = backToMenu.getItemMeta();
        backToMenuMeta.setDisplayName(ChatColor.RED + "Back to Reputation Screen");
        backToMenu.setItemMeta(backToMenuMeta);
        gui.setItem(53, backToMenu);
        player.openInventory(gui);
    }
}
