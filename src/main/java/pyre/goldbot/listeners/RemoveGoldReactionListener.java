package pyre.goldbot.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.reaction.ReactionRemoveEvent;
import org.javacord.api.listener.message.reaction.ReactionRemoveListener;
import pyre.goldbot.GoldBot;
import pyre.goldbot.GoldManager;
import pyre.goldbot.operation.RemoveGoldOperation;

import java.util.Arrays;

public class RemoveGoldReactionListener implements ReactionRemoveListener {

    private static final Logger logger = LogManager.getLogger(RemoveGoldReactionListener.class);

    @Override
    public void onReactionRemove(ReactionRemoveEvent event) {
        if (!event.getEmoji().equalsEmoji(GoldBot.getGoldEmoji())) {
            return;
        }
        Message message = event.getApi().getMessageById(event.getMessageId(), event.getChannel()).join();
        if (message == null) {
            logger.error("Cannot remove gold. Message {} does not exist!", event.getMessageId());
            return;
        }
        RemoveGoldOperation removeGoldOperation = new RemoveGoldOperation(event.getApi(), event.getChannel().getIdAsString(),
                message.getCreationTimestamp(), message.getAuthor().getIdAsString());
        GoldManager.getInstance().addOperations(Arrays.asList(removeGoldOperation));
    }
}
