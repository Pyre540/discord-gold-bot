package pyre.goldbot.operation;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.Reaction;
import pyre.goldbot.GoldBot;
import pyre.goldbot.entity.GoldCollector;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

public class RemoveGoldOperation extends Operation {

    private String userId;
    private Message message;

    public RemoveGoldOperation(DiscordApi api, String channelId, Instant msgTimestamp, String userId, Message message) {
        super(api, channelId, msgTimestamp);
        this.userId = userId;
        this.message = message;
    }

    @Override
    public void execute(Map<String, GoldCollector> goldCollectors) {
        GoldCollector goldCollector = goldCollectors.computeIfAbsent(userId, GoldCollector::new);
        goldCollector.decreaseScore();
        Optional<Reaction> goldReaction = message.getReactionByEmoji(GoldBot.getGoldEmoji());
        if (!goldReaction.isPresent()) {
            goldCollector.getGoldMessages().remove(message.getLink());
        }
    }
}
