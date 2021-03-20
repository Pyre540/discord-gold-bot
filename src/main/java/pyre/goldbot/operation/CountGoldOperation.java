package pyre.goldbot.operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import pyre.goldbot.GoldBot;
import pyre.goldbot.db.GoldDao;
import pyre.goldbot.db.entity.GoldCollector;
import pyre.goldbot.db.entity.GoldMessage;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CountGoldOperation extends Operation {

    private static final Logger logger = LogManager.getLogger(CountGoldOperation.class);

    private int id;
    private int totalOperations;

    public CountGoldOperation(DiscordApi api, String channelId, int id, int totalOperations, Message message) {
        super(api, channelId, message);
        this.id = id;
        this.totalOperations = totalOperations;
    }

    @Override
    public void execute() {
        ServerTextChannel textChannel = api.getServerTextChannelById(channelId).orElse(null);
        if (textChannel == null) {
            logger.error("Text channel {} does not exist!", channelId);
            return;
        }

        message.edit(GoldBot.getMessage("countGold.step", id, totalOperations));
        List<Message> goldMessages = textChannel.getMessagesBeforeAsStream(message)
                .filter(m -> m.getReactionByEmoji(GoldBot.getGoldEmoji()).isPresent())
                .collect(Collectors.toList());
        Map<String, GoldCollector> goldCollectors = GoldDao.getInstance().getGoldCollectorsWithMessagesAsMap();
        for (Message msg : goldMessages) {
            msg.getReactionByEmoji(GoldBot.getGoldEmoji()).ifPresent(r -> {
                GoldCollector goldCollector =
                        goldCollectors.computeIfAbsent(msg.getAuthor().getIdAsString(), GoldCollector::new);
                goldCollector.modifyGoldCount(r.getCount());
                GoldMessage newMessage = new GoldMessage(msg.getIdAsString(), goldCollector,
                        msg.getCreationTimestamp(), r.getCount(), msg.getLink().toString());
                goldCollector.addGoldMessage(newMessage);
            });
        }
        GoldDao.getInstance().saveOrUpdate(goldCollectors.values());
    }
}
