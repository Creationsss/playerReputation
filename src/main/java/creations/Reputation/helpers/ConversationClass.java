package creations.Reputation.helpers;

import creations.Reputation.main;
import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class ConversationClass extends StringPrompt {
    DatabaseManager databaseManager = new DatabaseManager();
    main plugin = main.getPlugin(main.class);


    @Override
    public String getPromptText(ConversationContext conversationContext) {
        conversationContext.getForWhom().sendRawMessage(plugin.colorize("&ePlease enter your &6&l&nreasoning"));
        return plugin.colorize("&eTo stop type &6&l&ncancel");
    }

    @Override
    public Prompt acceptInput(ConversationContext conversationContext, String s) {
        List<Object> values = databaseManager.ReturnStaticCurrentInfo();

        Player sender = (Player) values.get(2);
        String nameglob = values.get(0).toString();
        UUID playerUUID = UUID.fromString(values.get(4).toString());
        String command = values.get(1).toString();
        Player playerCALL = sender;

        if (s.equals("cancel")) {

            conversationContext.getForWhom().sendRawMessage(plugin.colorize("&eCanceled giving reputation to &l&6" + nameglob));
            return Prompt.END_OF_CONVERSATION;
        }

        databaseManager.updateREP(nameglob, command, playerCALL, s, playerUUID);
        command = command.equals("Positively") ? "Positive" : "Negative";
        conversationContext.getForWhom().sendRawMessage(plugin.colorize("&eSuccessfully gave &6&l" + nameglob + " &e&n" + command + "&r&e Reputation"));
        conversationContext.getForWhom().sendRawMessage(plugin.colorize("&eReason&8&f: &6&n" + s));
        return END_OF_CONVERSATION;
    }
}
