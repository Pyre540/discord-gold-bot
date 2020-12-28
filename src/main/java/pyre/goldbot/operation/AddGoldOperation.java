package pyre.goldbot.operation;

import org.javacord.api.DiscordApi;
import pyre.goldbot.entity.GoldCollector;

import java.time.Instant;
import java.util.Map;

public class AddGoldOperation extends Operation {

    private String userId;

    public AddGoldOperation(DiscordApi api, String channelId, Instant msgTimestamp, String userId) {
        super(api, channelId, msgTimestamp);
        this.userId = userId;
    }

    @Override
    public void execute(Map<String, GoldCollector> goldCollectors) {
        GoldCollector goldCollector = goldCollectors.computeIfAbsent(userId, GoldCollector::new);
        goldCollector.increaseScore();
    }
}
