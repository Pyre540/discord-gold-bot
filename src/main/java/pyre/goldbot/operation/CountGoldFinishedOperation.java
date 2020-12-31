package pyre.goldbot.operation;

import org.javacord.api.entity.message.Message;
import pyre.goldbot.GoldBot;
import pyre.goldbot.entity.GoldCollector;

import java.time.Instant;
import java.util.Map;

public class CountGoldFinishedOperation extends Operation {

    private Message message;

    public CountGoldFinishedOperation(Message message) {
        super(null, "-1", Instant.MAX);
        this.message = message;
    }

    @Override
    public void execute(Map<String, GoldCollector> goldCollectors) {
        message.edit(GoldBot.getMessages().getString("countGold.finished"));
    }
}
