package pyre.goldbot.operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import pyre.goldbot.GoldBot;
import pyre.goldbot.db.GoldDao;
import pyre.goldbot.db.entity.GoldCollector;
import pyre.goldbot.db.entity.GoldMessage;

import java.util.Optional;

public class RemoveGoldOperation extends Operation {

    private static final Logger logger = LogManager.getLogger(RemoveGoldOperation.class);

    private String userId;

    public RemoveGoldOperation(DiscordApi api, String channelId, String userId, Message message) {
        super(api, channelId, message);
        this.userId = userId;
    }

    @Override
    public void execute() {
        GoldCollector goldCollector = GoldDao.getInstance().getGoldCollectorWithMessages(userId);
        if (goldCollector == null) {
            logger.error("Cannot remove gold from User! User {} does not exist!", userId);
            return;
        }
        Optional<GoldMessage> msg = goldCollector.getGoldMessages().stream()
                .filter(m -> m.getMessageId().equals(this.message.getIdAsString()))
                .findFirst();
        if (!msg.isPresent()) {
            logger.error("Cannot remove gold from Message! Message {} is not related to User {}!",
                    this.message.getIdAsString(), userId);
            return;
        }

        GoldMessage goldMessage = msg.get();
        goldMessage.decreaseMessageGold();
        if (goldMessage.getMessageGold() == 0) {
            goldCollector.getGoldMessages().remove(goldMessage);
        }
        goldCollector.decreaseGoldCount();
        GoldDao.getInstance().saveOrUpdate(goldCollector);

        if (goldCollector.getGoldCount() == 69) {
            Server server = GoldBot.getApi().getServers().iterator().next();
            User user = GoldBot.getApi().getUserById(goldCollector.getUserId()).join();
            new MessageBuilder()
                    .append(GoldBot.getMessage("usersGold.nice", user.getDisplayName(server),
                            GoldBot.getGoldEmoji().getMentionTag()))
                    .send(GoldBot.getMainChannel());
        }
    }
}
