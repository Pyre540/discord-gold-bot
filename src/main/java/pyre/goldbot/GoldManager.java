package pyre.goldbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import pyre.goldbot.db.GoldDao;
import pyre.goldbot.db.entity.GoldCollector;
import pyre.goldbot.db.entity.GoldMessage;
import pyre.goldbot.operation.CountGoldOperation;
import pyre.goldbot.operation.Operation;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static pyre.goldbot.GoldBot.CROWN;

public class GoldManager {

    private static final Logger logger = LogManager.getLogger(GoldManager.class);

    private static final GoldManager INSTANCE = new GoldManager();

    private Map<String, Instant> channelsToCount = new ConcurrentHashMap<>();
    private BlockingQueue<Operation> operationQueue = new LinkedBlockingQueue<>();

    private SortedMap<Integer, List<GoldCollector>> ranking = new TreeMap<>();

    private List<GoldCollector> rankingLeaders = new ArrayList<>();

    private Predicate<Operation> channelAbsent = o -> !channelsToCount.containsKey(o.getChannelId());
    private Predicate<Operation> simpleOperationAfter = o -> !(o instanceof CountGoldOperation)
                    && channelsToCount.get(o.getChannelId()).isBefore(o.getMsgTimestamp());

    private GoldManager() {
        new Thread(() -> {
            try {
                while (true) {
                    Operation operation = operationQueue.take();
                    operation.execute();
                    synchronized (this) {
                        if (operation instanceof CountGoldOperation) {
                            channelsToCount.remove(operation.getChannelId());
                        }
                        if (channelsToCount.isEmpty()) {
                            updateRanking();
                        }
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

    public synchronized void initRanking() {
        rankingLeaders = GoldDao.getInstance().getLeaders();
        rankingLeaders.sort(GoldCollector::compareTo);

        Map<String, GoldCollector> goldCollectors = new HashMap<>();
        Collection<ServerTextChannel> textChannels = GoldBot.getApi().getServerTextChannels();
        for (ServerTextChannel textChannel : textChannels) {
            List<Message> goldMessages = textChannel.getMessagesAsStream()
                    .filter(m -> m.getReactionByEmoji(GoldBot.getGoldEmoji()).isPresent())
                    .collect(Collectors.toList());
            for (Message msg : goldMessages) {
                msg.getReactionByEmoji(GoldBot.getGoldEmoji()).ifPresent(r -> {
                    GoldCollector goldCollector =
                            goldCollectors.computeIfAbsent(msg.getAuthor().getIdAsString(), GoldCollector::new);
                    goldCollector.addGoldMessage(new GoldMessage(msg.getIdAsString(), goldCollector,
                            msg.getCreationTimestamp(), r.getCount(), msg.getLink().toString()));
                    goldCollector.modifyGoldCount(r.getCount());
                });
            }
        }
        GoldDao.getInstance().updateGoldCollectors(goldCollectors);
        updateRanking();
    }

    public synchronized boolean addOperations(List<Operation> operations) {
        boolean opsAdded = false;
        if (!operations.stream().allMatch(channelAbsent.or(simpleOperationAfter))) {
            return false;
        }
        for (Operation operation : operations) {
            if (operation instanceof CountGoldOperation) {
                channelsToCount.put(operation.getChannelId(), operation.getMsgTimestamp());
            }
            try {
                operationQueue.put(operation);
                opsAdded = true;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Failed to add operation: {}", operation);
                return opsAdded;
            }
        }
        return opsAdded;
    }

    public synchronized SortedMap<Integer, List<GoldCollector>> getRanking() {
        return new TreeMap<>(ranking);
    }

    private void updateRanking() {
        ranking.clear();
        List<GoldCollector> goldCollectors = GoldDao.getInstance().getGoldCollectors();
        goldCollectors.stream()
                .sorted(Comparator.reverseOrder())
                .forEach(item -> {
                    Integer score = item.getGoldCount();
                    if (score == 0) {
                        return;
                    }
                    if (ranking.isEmpty()) {
                        ranking.put(1, new ArrayList<>());
                    } else {
                        Integer rank = ranking.lastKey();
                        List<GoldCollector> items = ranking.get(rank);
                        if (!score.equals(items.get(0).getGoldCount())) {
                            ranking.put(rank + items.size(), new ArrayList<>());
                        }
                    }
                    ranking.get(ranking.lastKey()).add(item);
                });

        List<GoldCollector> newLeaders = ranking.getOrDefault(1, new ArrayList<>());
        newLeaders.sort(GoldCollector::compareTo);
        if (!rankingLeaders.equals(newLeaders)) {
            rankingLeaders = newLeaders;
            announceNewLeaders();
            updateUserNicknames();
        }
    }

    private void announceNewLeaders() {
        String msg;
        if (rankingLeaders.isEmpty() || rankingLeaders.get(0).getGoldCount() == 0) {
            msg = GoldBot.getMessage("announcement.noLeaders", CROWN);
        } else {
            DiscordApi api = GoldBot.getApi();
            Server server = api.getServers().iterator().next();
            String leaders = rankingLeaders.stream()
                    .map(l -> api.getUserById(l.getUserId()).join()
                            .getDisplayName(server)
                            .replace(GoldBot.CROWN, "")
                            .trim())
                    .collect(Collectors.joining(", "));
            if (rankingLeaders.size() > 1) {
                msg = GoldBot.getMessage("announcement.multipleLeaders", CROWN, leaders);
            } else {
                msg = GoldBot.getMessage("announcement.singleLeader." + rankingLeaders.get(0).getPronouns(), CROWN,
                        leaders);
            }
        }
        new MessageBuilder().append(msg).send(GoldBot.getMainChannel());
    }

    private void updateUserNicknames() {
        DiscordApi api = GoldBot.getApi();
        Server server = api.getServers().iterator().next();
        Collection<User> members = server.getMembers();
        List<String> leaders = rankingLeaders.stream()
                .map(GoldCollector::getUserId)
                .collect(Collectors.toList());
        for (User member : members) {
            if (server.getOwnerId() == member.getId()) {
                continue;
            }
            String displayName = member.getDisplayName(server);
            if (leaders.contains(member.getIdAsString()) && !displayName.endsWith(CROWN)) {
                member.updateNickname(server, displayName + " " + CROWN).join();
            } else {
                String nickname = member.getNickname(server).orElse("");
                if (nickname.endsWith(CROWN) && !leaders.contains(member.getIdAsString())) {
                    String newNickname = nickname.substring(0, nickname.lastIndexOf(CROWN)).trim();
                    member.updateNickname(server, newNickname).join();
                }
            }
        }
    }
}
