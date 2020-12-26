package pyre.goldbot.operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import pyre.goldbot.GoldBot;
import pyre.goldbot.GoldManager;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CountGoldOperation extends Operation {

    private static final Logger logger = LogManager.getLogger(CountGoldOperation.class);

    private int id;
    private int totalOperations;
    private Message message;

    public CountGoldOperation(DiscordApi api, String channelId, Instant msgTimestamp, int id, int totalOperations,
                              Message message) {
        super(api, channelId, msgTimestamp);
        this.id = id;
        this.totalOperations = totalOperations;
        this.message = message;
    }

    @Override
    public void execute(Map<String, GoldManager.GoldCollector> goldCollectors) {
        ServerTextChannel textChannel = api.getServerTextChannelById(channelId).orElse(null);
        if (textChannel == null) {
            logger.error("Text channel {} does not exist!", channelId);
            return;
        }
        message.edit(String.format("Przeliczam z\u0142oto [%d/%d]...", id, totalOperations));
        List<Message> goldMessages = textChannel.getMessagesBeforeAsStream(message)
                .filter(m -> m.getReactionByEmoji(GoldBot.GOLD_EMOJI).isPresent())
                .collect(Collectors.toList());
        for (Message msg : goldMessages) {
            msg.getReactionByEmoji(GoldBot.GOLD_EMOJI).ifPresent(r -> {
                GoldManager.GoldCollector goldCollector =
                        goldCollectors.computeIfAbsent(msg.getAuthor().getIdAsString(), GoldManager.GoldCollector::new);
                goldCollector.modifyScore(r.getCount());
            });
        }
    }
}
