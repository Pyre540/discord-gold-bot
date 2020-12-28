package pyre.goldbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import pyre.goldbot.entity.GoldCollector;
import pyre.goldbot.operation.CountGoldFinishedOperation;
import pyre.goldbot.operation.CountGoldOperation;
import pyre.goldbot.operation.Operation;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GoldManager {

    private static final Logger logger = LogManager.getLogger(GoldManager.class);

    private static final GoldManager INSTANCE = new GoldManager();

    private Map<String, Instant> channelsToCount = new ConcurrentHashMap<>();
    private BlockingQueue<Operation> operationQueue = new LinkedBlockingQueue<>();

    private Map<String, GoldCollector> goldCollectors = new HashMap<>();
    private SortedMap<Integer, List<GoldCollector>> ranking = new TreeMap<>();

    private Predicate<Operation> channelAbsent = o -> !channelsToCount.containsKey(o.getChannelId());
    private Predicate<Operation> simpleOperationAfter = o -> !(o instanceof CountGoldOperation)
            && channelsToCount.get(o.getChannelId()).isBefore(o.getMsgTimestamp());

    private GoldManager() {
        new Thread(() -> {
            try {
                while (true) {
                    Operation operation = operationQueue.take();
                    operation.execute(goldCollectors);
                    synchronized (this) {
                        if (operation instanceof CountGoldOperation) {
                            channelsToCount.remove(operation.getChannelId());
                        }
                        updateRanking();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Failed to execute operation.", e);
            }
        }).start();
    }

    public static GoldManager getInstance() {
        return INSTANCE;
    }

    public synchronized void initRanking(DiscordApi api) {
        Collection<ServerTextChannel> textChannels = api.getServerTextChannels();
        for (ServerTextChannel textChannel : textChannels) {
            List<Message> goldMessages = textChannel.getMessagesAsStream()
                    .filter(m -> m.getReactionByEmoji(GoldBot.GOLD_EMOJI).isPresent())
                    .collect(Collectors.toList());
            for (Message msg : goldMessages) {
                msg.getReactionByEmoji(GoldBot.GOLD_EMOJI).ifPresent(r -> {
                    GoldCollector goldCollector =
                            goldCollectors.computeIfAbsent(msg.getAuthor().getIdAsString(), GoldCollector::new);
                    goldCollector.modifyScore(r.getCount());
                });
            }
        }
        updateRanking();
    }

    public synchronized int addOperations(List<Operation> operations) {
        int opsAdded = 0;
        for (Operation operation : operations) {
            if (channelAbsent.or(simpleOperationAfter).test(operation)) {
                if (operation instanceof CountGoldOperation) {
                    channelsToCount.put(operation.getChannelId(), operation.getMsgTimestamp());
                }
                if (opsAdded == 0 && operation instanceof CountGoldFinishedOperation) {
                    return 0;
                }
                try {
                    operationQueue.put(operation);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.warn("Failed to add operation: {}", operation);
                    return opsAdded;
                }
                opsAdded++;
            }
        }
        return opsAdded;
    }

    public synchronized SortedMap<Integer, List<GoldCollector>> getRanking() {
        return new TreeMap<>(ranking);
    }

    private void updateRanking() {
        ranking.clear();
        goldCollectors.values().stream()
                .sorted(Comparator.reverseOrder())
                .forEach(item -> {
                    Integer score = item.getScore();
                    if (ranking.isEmpty()) {
                        ranking.put(1, new ArrayList<>());
                    } else {
                        Integer rank = ranking.lastKey();
                        List<GoldCollector> items = ranking.get(rank);
                        if (!score.equals(items.get(0).getScore())) {
                            ranking.put(rank + items.size(), new ArrayList<>());
                        }
                    }
                    ranking.get(ranking.lastKey()).add(item);
                });
    }
}
