package pyre.goldbot.operation;

import org.javacord.api.DiscordApi;
import pyre.goldbot.entity.GoldCollector;

import java.time.Instant;
import java.util.Map;

public abstract class Operation {

    protected DiscordApi api;
    protected String channelId;
    protected Instant msgTimestamp;

    public Operation(DiscordApi api, String channelId, Instant msgTimestamp) {
        this.api = api;
        this.channelId = channelId;
        this.msgTimestamp = msgTimestamp;
    }

    public abstract void execute(Map<String, GoldCollector> goldCollectors);

    public String getChannelId() {
        return channelId;
    }

    public Instant getMsgTimestamp() {
        return msgTimestamp;
    }
}
