package pyre.goldbot.operation;

import org.javacord.api.DiscordApi;
import pyre.goldbot.entity.GoldCollector;

import java.net.URL;
import java.time.Instant;
import java.util.Map;

public class AddGoldOperation extends Operation {

    private String userId;
    private URL msgUrl;

    public AddGoldOperation(DiscordApi api, String channelId, Instant msgTimestamp, String userId, URL msgUrl) {
        super(api, channelId, msgTimestamp);
        this.userId = userId;
        this.msgUrl = msgUrl;
    }

    @Override
    public void execute(Map<String, GoldCollector> goldCollectors) {
        GoldCollector goldCollector = goldCollectors.computeIfAbsent(userId, GoldCollector::new);
        goldCollector.increaseScore();
        goldCollector.getGoldMessages().add(msgUrl);
    }
}
