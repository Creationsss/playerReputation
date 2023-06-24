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
        if(!databaseManager.Disabled) {
            Player player = PJ.getPlayer();
            UUID PlayerUUID = player.getUniqueId();
            Boolean bol = new ConfigClass().checkConfig();
            databaseManager.createPlayer(PlayerUUID, player);
        }
    }

    @EventHandler
    public void clickevent(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory clickedInventory = e.getClickedInventory();
        List<Object> staticItems = databaseManager.ReturnStaticCurrentInfo();

        Player LocalizedSender = (Player) staticItems.get(2);
        String LocalizedPlayer = (String) staticItems.get(0);

        Material backButton = ConfigClass.backButton;
        Material nextButton = ConfigClass.nextButton;

        Conversation s = getConversationFactory().withFirstPrompt(new ConversationClass()).thatExcludesNonPlayersWithMessage("Cannot Be used Inside the console").withLocalEcho(false).buildConversation(player);
        if (clickedInventory == null) {
            return;
        }
        ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null) {
            return;
        }
        if (e.getView().getTitle().equals(ConfigClass.reputationMenuName)) {
            Material givenItem = ConfigClass.givenItem;
            Material sentItem = ConfigClass.sentItem;
            Material serverSentItem = ConfigClass.serverSentItem;
            Material positiveButton = ConfigClass.positiveButton;
            Material negativeButton = ConfigClass.negativeButton;

            if(clickedItem.getType().equals(givenItem))
            {
                allReputationPlayerGUI(LocalizedSender, LocalizedPlayer, 0);
            } else if (clickedItem.getType().equals(sentItem)) {
                allReputationPlayerSentGUI(LocalizedSender, LocalizedPlayer, 0);
            }else if (clickedItem.getType().equals(serverSentItem)) {
                allPlayerREPSGUI(player,0);
            } else if (clickedItem.getType().equals(positiveButton)) {
                databaseManager.SetStaticCurrentInfo(null, "Positively", null, null, null);
                s.begin();
                e.getView().close();
            }else if (clickedItem.getType().equals(negativeButton)) {
                databaseManager.SetStaticCurrentInfo(null, "Negatively", null, null, null);
                s.begin();
                e.getView().close();
            }
            e.setCancelled(true);
        } else if (e.getView().getTitle().equals(main.replaceValues(ConfigClass.playerReputation,"name" ,Collections.singletonList(LocalizedPlayer))) || (e.getView().getTitle().equals(main.replaceValues(ConfigClass.sentReputation, "name" ,Collections.singletonList(LocalizedPlayer))))) {
             if (clickedItem.getType().equals(backButton)) {
                 CreateMainGUI(LocalizedSender, LocalizedPlayer);
             } else if (clickedItem.getType().equals(nextButton)) {
                 String buttonName = clickedItem.getItemMeta().getDisplayName();
                 currentPage += buttonName.startsWith(ChatColor.GREEN + "Next Page") ? 1 : 0;
                 currentPage -= buttonName.startsWith(ChatColor.GREEN + "Previous Page") ? 1 : 0;
                 switch (currentMenu) {
                     case 0-> allReputationPlayerGUI(player, LocalizedPlayer, currentPage);
                     case 1-> allReputationPlayerSentGUI(LocalizedSender, LocalizedPlayer, currentPage);
                 }
             }else if (clickedItem.getType().equals(Material.PLAYER_HEAD)){
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
        } else if (e.getView().getTitle().equals(ConfigClass.allReputation)) {
            if (clickedItem.getType().equals(backButton)) {
                CreateMainGUI(LocalizedSender, LocalizedPlayer);
            }else if (clickedItem.getType().equals(nextButton)){
                String buttonName = clickedItem.getItemMeta().getDisplayName();
                currentPage += buttonName.startsWith(ChatColor.GREEN + "Next Page") ? 1 : 0;
                currentPage -= buttonName.startsWith(ChatColor.GREEN + "Previous Page") ? 1 : 0;
                allReputationPlayerGUI(player, LocalizedPlayer, currentPage);
            } else if (clickedItem.getType().equals(Material.PLAYER_HEAD)){
                CreateMainGUI(LocalizedSender, clickedItem.getItemMeta().getDisplayName());

            }
            e.setCancelled(true);
        }else if (e.getView().getTitle().equals(ConfigClass.confirmationMenuName)) {
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
        Inventory gui = Bukkit.createInventory(player, 9, ConfigClass.confirmationMenuName);
        Material backButton = ConfigClass.backButton;

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

        ItemStack cancelDeletion = new ItemStack(backButton);
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
        Material givenItem = ConfigClass.givenItem;
        Material sentItem = ConfigClass.sentItem;
        Material serverSentItem = ConfigClass.serverSentItem;
        Material positiveButton = ConfigClass.positiveButton;
        Material negativeButton = ConfigClass.negativeButton;
        Material blockedButton = ConfigClass.blockedButton;

        List<String> repItems = databaseManager.GetAllReputationPlayerInfo(name);
        Inventory gui = Bukkit.createInventory(player, 9, ConfigClass.reputationMenuName);

        UUID uuid = databaseManager.getUUIDFromName(name);
        databaseManager.SetStaticCurrentInfo(name, null, player, null, uuid);
        List<Object> staticItems = databaseManager.ReturnStaticCurrentInfo();

        Player LocalizedSender = (Player) staticItems.get(2);
        String LocalizedPlayer = (String) staticItems.get(0);


        ItemStack playerSkull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerSkull.getItemMeta();
        skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
        skullMeta.setDisplayName(name);

        String mainGUIHeadMessage = main.replaceValues(ConfigClass.mainGUIHeadMessage, "MainGUIHeadMessage", Arrays.asList(repItems.get(0), repItems.get(1), repItems.get(2), repItems.get(3)));

        String[] SplitMainGUIHeadMessage = mainGUIHeadMessage.split(",");

        ArrayList<String> playerLore = new ArrayList<>();
        for (String item : SplitMainGUIHeadMessage) {
            playerLore.add(item);
        }
        skullMeta.setLore(playerLore);
        playerSkull.setItemMeta(skullMeta);

        ItemStack positive = new ItemStack(positiveButton);
        ItemMeta checkmarkMeta = positive.getItemMeta();
        checkmarkMeta.setDisplayName(ConfigClass.positiveButtonText);
        positive.setItemMeta(checkmarkMeta);

        ItemStack negative = new ItemStack(negativeButton);
        ItemMeta negativeMeta = negative.getItemMeta();
        negativeMeta.setDisplayName(ConfigClass.negativeButtonText);
        negative.setItemMeta(negativeMeta);

        ItemStack AllReputation = new ItemStack(givenItem);
        ItemMeta AllReputationMeta = AllReputation.getItemMeta();
        AllReputationMeta.setDisplayName(main.replaceValues(ConfigClass.givenItemText, "name" ,Collections.singletonList(name)));

        AllReputation.setItemMeta(AllReputationMeta);

        ItemStack AllReputationSent = new ItemStack(sentItem);
        ItemMeta AllReputationSentMeta = AllReputationSent.getItemMeta();
        AllReputationSentMeta.setDisplayName(main.replaceValues(ConfigClass.sentItemText, "name" ,Collections.singletonList(name)));
        AllReputationSent.setItemMeta(AllReputationSentMeta);

        ItemStack AllReputationSERVER = new ItemStack(serverSentItem);
        ItemMeta AllReputationSERVERMeta = AllReputationSERVER.getItemMeta();
        AllReputationSERVERMeta.setDisplayName(ConfigClass.serverSentItemText);
        AllReputationSERVER.setItemMeta(AllReputationSERVERMeta);

        ItemStack cannotREP = new ItemStack(blockedButton);
        ItemMeta cannotREPMeta = cannotREP.getItemMeta();
        cannotREPMeta.setDisplayName(ConfigClass.blockedButtonText);
        cannotREP.setItemMeta(cannotREPMeta);

        int moveIcons = 3;

        ConfigClass configClass = new ConfigClass();

        int CoolDown = Integer.parseInt(configClass.returnOthers().get(0));
        HashMap<String, Long> cooldowns  = databaseManager.giveRepCoolDowns;

        if(cooldowns.containsKey(player.getName())) {
            long secondsLeft = ((cooldowns.get(player.getName()) / 1000) + CoolDown) - (System.currentTimeMillis() / 1000);
            int secondsLeftINT = Math.toIntExact(secondsLeft);
            String hours = Integer.toString(secondsLeftINT / 3600);
            String minutes = Integer.toString(secondsLeftINT % 3600 / 60);
            String seconds = Integer.toString(secondsLeftINT % 60);

            ItemStack LampINFO = new ItemStack(blockedButton);
            ItemMeta LampINFOMeta = LampINFO.getItemMeta();
            String localString = main.replaceValues(ConfigClass.timerMessage,"Time", Arrays.asList(hours, minutes, seconds));
            LampINFOMeta.setDisplayName(localString);
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

        Material backButton = ConfigClass.backButton;
        Material nextButton = ConfigClass.nextButton;
        Inventory gui = Bukkit.createInventory(player, 54, main.replaceValues(ConfigClass.playerReputation, "name" ,Collections.singletonList(name)));

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

            String mainGUIHeadMessage = main.replaceValues(ConfigClass.playersGivenGUIHeadMessage, "playerREP", Arrays.asList(polarity, repID, reason));

            String[] SplitMainGUIHeadMessage = mainGUIHeadMessage.split(",");

            ArrayList<String> playerLore = new ArrayList<>();
            for (String item : SplitMainGUIHeadMessage) {
                playerLore.add(item);
            }

            meta.setLore(playerLore);
            playerskull.setItemMeta(meta);

            gui.addItem(playerskull);
        }
        if (currentPage > 0) {
            ItemStack previousPageItem = new ItemStack(nextButton);
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
            ItemStack nextPageItem = new ItemStack(nextButton);
            ItemMeta nextPageMeta = nextPageItem.getItemMeta();
            nextPageMeta.setDisplayName(ChatColor.GREEN + "Next Page");
            ArrayList<String> skullLore = new ArrayList<>();
            skullLore.add("Current page " + localPageSTR + " out of " + PageCount);
            nextPageMeta.setLore(skullLore);

            nextPageItem.setItemMeta(nextPageMeta);
            gui.setItem(52, nextPageItem);
        }
        ItemStack backToMenu = new ItemStack(backButton);
        ItemMeta backToMenuMeta = backToMenu.getItemMeta();
        backToMenuMeta.setDisplayName(ConfigClass.backButtonText);
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

        Inventory gui = Bukkit.createInventory(player, 54, main.replaceValues(ConfigClass.sentReputation, "name" ,Collections.singletonList(name)));
        Material backButton = ConfigClass.backButton;
        Material nextButton = ConfigClass.nextButton;

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
            ItemStack previousPageItem = new ItemStack(nextButton);
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
            ItemStack nextPageItem = new ItemStack(nextButton);
            ItemMeta nextPageMeta = nextPageItem.getItemMeta();
            nextPageMeta.setDisplayName(ChatColor.GREEN + "Next Page");
            ArrayList<String> skullLore = new ArrayList<>();
            skullLore.add("Current page " + localPageSTR + " out of " + PageCount);
            nextPageMeta.setLore(skullLore);

            nextPageItem.setItemMeta(nextPageMeta);
            gui.setItem(52, nextPageItem);
        }
        ItemStack backToMenu = new ItemStack(backButton);
        ItemMeta backToMenuMeta = backToMenu.getItemMeta();
        backToMenuMeta.setDisplayName(ConfigClass.backButtonText);
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
        Material backButton = ConfigClass.backButton;
        Material nextButton = ConfigClass.nextButton;

        Inventory gui = Bukkit.createInventory(player, 54, ConfigClass.allReputation);
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

            String mainGUIHeadMessage = main.replaceValues(ConfigClass.playersGUIHeadMessage, "MainGUIHeadMessage", Arrays.asList(posrep, negrep, overall, standing));

            String[] SplitMainGUIHeadMessage = mainGUIHeadMessage.split(",");

            ArrayList<String> playerLore = new ArrayList<>();
            for (String item : SplitMainGUIHeadMessage) {
                playerLore.add(item);
            }
            meta.setLore(playerLore);
            playerskull.setItemMeta(meta);
            gui.addItem(playerskull);
        }
        if (currentPage > 0) {
            ItemStack previousPageItem = new ItemStack(nextButton);
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
            ItemStack nextPageItem = new ItemStack(nextButton);
            ItemMeta nextPageMeta = nextPageItem.getItemMeta();
            nextPageMeta.setDisplayName(ChatColor.GREEN + "Next Page");
            ArrayList<String> skullLore = new ArrayList<>();
            skullLore.add("Current page " + localPageSTR + " out of " + PageCount);
            nextPageMeta.setLore(skullLore);

            nextPageItem.setItemMeta(nextPageMeta);
            gui.setItem(52, nextPageItem);
        }
        ItemStack backToMenu = new ItemStack(backButton);
        ItemMeta backToMenuMeta = backToMenu.getItemMeta();
        backToMenuMeta.setDisplayName(ConfigClass.backButtonText);
        backToMenu.setItemMeta(backToMenuMeta);
        gui.setItem(53, backToMenu);
        player.openInventory(gui);
    }
}
