package pyre.goldbot.commands;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import pyre.goldbot.GoldBot;
import pyre.goldbot.GoldManager;
import pyre.goldbot.entity.GoldCollector;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public class UsersGoldCommand implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageAuthor().isBotUser()) {
            return;
        }
        if (!event.getMessageContent().matches("^!usersGold ([\\w ])*$")) {
            return;
        }
        String username = event.getMessageContent().substring(event.getMessageContent().indexOf(' ')).trim();

        Server server = GoldBot.getApi().getServers().iterator().next();
        Collection<User> members = server.getMembers();

        User user = null;
        for (User member : members) {
            if (member.getDisplayName(server).replace(GoldBot.CROWN, "").trim().equalsIgnoreCase(username)) {
                user = member;
                break;
            }
        }
        if (user == null) {
            event.getChannel().sendMessage(GoldBot.getMessages().getString("usersGold.noUser"));
            return;
        }

        Map<String, GoldCollector> goldCollectors = GoldManager.getInstance().getGoldCollectors();
        GoldCollector goldCollector = goldCollectors.get(user.getIdAsString());
        if (goldCollector != null && !goldCollector.getGoldMessages().isEmpty()) {
            String links = goldCollector.getGoldMessages().stream()
                    .map(URL::toString)
                    .collect(Collectors.joining("\n"));
            new MessageBuilder().append(String.format(GoldBot.getMessages().getString("usersGold.gold"),
                    user.getDisplayName(server)))
                    .appendNewLine()
                    .append(links)
                    .send(event.getChannel());
        } else {
            new MessageBuilder().append(String.format(GoldBot.getMessages().getString("usersGold.noGold"),
                    user.getDisplayName(server)))
                    .send(event.getChannel());
        }
    }
}
