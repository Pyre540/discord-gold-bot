package pyre.goldbot.operation;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import pyre.goldbot.db.GoldDao;
import pyre.goldbot.db.entity.GoldCollector;
import pyre.goldbot.db.entity.GoldMessage;

import java.util.Optional;

public class AddGoldOperation extends Operation {

    public AddGoldOperation(DiscordApi api, String channelId, Message message) {
        super(api, channelId, message);
    }

    @Override
    public void execute() {
        GoldCollector goldCollector =
                GoldDao.getInstance().getGoldCollectorWithMessages(message.getAuthor().getIdAsString());
        if (goldCollector == null) {
            goldCollector = new GoldCollector(message.getAuthor().getIdAsString());
        }
        goldCollector.increaseGoldCount();
        Optional<GoldMessage> message = goldCollector.getGoldMessages().stream()
                .filter(m -> m.getMessageId().equals(this.message.getIdAsString()))
                .findFirst();
        GoldMessage goldMessage;
        if(!message.isPresent()) {
            goldMessage = new GoldMessage(this.message.getIdAsString(), goldCollector,
                    this.message.getCreationTimestamp(), 0, this.message.getLink().toString());
            goldCollector.addGoldMessage(goldMessage);
        } else {
            goldMessage = message.get();
        }
        goldMessage.increaseMessageGold();
        GoldDao.getInstance().saveOrUpdate(goldCollector);
    }
}
