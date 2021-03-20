package pyre.goldbot.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.reaction.ReactionAddEvent;
import org.javacord.api.listener.message.reaction.ReactionAddListener;
import pyre.goldbot.GoldBot;
import pyre.goldbot.GoldManager;
import pyre.goldbot.operation.AddGoldOperation;

import java.util.Arrays;
import java.util.Random;

public class AddGoldReactionListener implements ReactionAddListener {

    private static final Logger logger = LogManager.getLogger(AddGoldReactionListener.class);

    @Override
    public void onReactionAdd(ReactionAddEvent event) {
        if (!event.getEmoji().equalsEmoji(GoldBot.getGoldEmoji())) {
            return;
        }
        Message message = event.getApi().getMessageById(event.getMessageId(), event.getChannel()).join();
        if (message == null) {
            logger.error("Cannot add gold. Message {} does not exist!", event.getMessageId());
            return;
        }
        AddGoldOperation addGoldOperation = new AddGoldOperation(event.getApi(), event.getChannel().getIdAsString(),
               message);
        GoldManager.getInstance().addOperations(Arrays.asList(addGoldOperation));

        if (message.getAuthor().isBotUser()) {
            Server server = GoldBot.getApi().getServers().iterator().next();
            String username = event.getUser().orElseThrow(RuntimeException::new).getDisplayName(server);
            int quote = new Random().nextInt(3) + 1;
            new MessageBuilder()
                    .append(GoldBot.getMessage("goldForBot." + quote, username, GoldBot.getKekmEmoji().getMentionTag()))
                    .send(GoldBot.getMainChannel());
        }
    }
}
