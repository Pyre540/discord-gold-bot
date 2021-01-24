package pyre.goldbot.operation;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;

import java.time.Instant;

public abstract class Operation {

    protected DiscordApi api;
    protected String channelId;
    protected Message message;

    public Operation(DiscordApi api, String channelId, Message message) {
        this.api = api;
        this.channelId = channelId;
        this.message = message;
    }

    public abstract void execute();

    public String getChannelId() {
        return channelId;
    }

    public Instant getMsgTimestamp() {
        return message != null ? message.getCreationTimestamp() : Instant.MAX;
    }
}
