package pyre.goldbot.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import pyre.goldbot.GoldBot;
import pyre.goldbot.GoldManager;
import pyre.goldbot.entity.GoldCollector;

import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

public class RankingCommand implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageAuthor().isBotUser()) {
            return;
        }
        if (!event.getMessageContent().equalsIgnoreCase("!ranking")) {
            return;
        }
        SortedMap<Integer, List<GoldCollector>> ranking = GoldManager.getInstance().getRanking();
        DiscordApi api = event.getApi();
        String rankingEntries = ranking.entrySet().stream()
                .map(e -> String.format("%d. %s(%d pkt.)", e.getKey(),
                        e.getValue().stream().map(v -> getUserDisplayName(api, v.getUserId())).collect(Collectors.joining(", ")),
                        e.getValue().get(0).getScore()))
                .collect(Collectors.joining("\n"));
        new MessageBuilder()
                .append(GoldBot.getMessages().getString("ranking.message"))
                .appendCode("java", rankingEntries.isEmpty() ? GoldBot.getMessages().getString("ranking.noRanking") :
                        rankingEntries)
                .send(event.getChannel());
    }

    private String getUserDisplayName(DiscordApi api, String id) {
        return api.getUserById(id).join().getDisplayName(api.getServers().iterator().next());
    }
}
