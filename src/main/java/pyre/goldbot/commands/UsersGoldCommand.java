package pyre.goldbot.commands;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import pyre.goldbot.GoldBot;
import pyre.goldbot.db.GoldDao;
import pyre.goldbot.db.entity.GoldCollector;
import pyre.goldbot.db.entity.GoldMessage;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
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
            event.getChannel().sendMessage(GoldBot.getMessage("usersGold.noUser"));
            return;
        }

        GoldCollector goldCollector = GoldDao.getInstance().getGoldCollectorWithMessages(user.getIdAsString());
        if (goldCollector == null || goldCollector.getGoldMessages().isEmpty()) {
            GoldCollector.Pronouns pronouns = goldCollector == null ? GoldCollector.Pronouns.M :
                    goldCollector.getPronouns();
            String msg = GoldBot.getMessage("usersGold.noGold." + pronouns, user.getDisplayName(server));
            new MessageBuilder().append(msg).send(event.getChannel());
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
                .withLocale(Locale.forLanguageTag("PL"))
                .withZone(ZoneId.systemDefault());

        MessageBuilder mb = new MessageBuilder()
                .append(GoldBot.getMessage("usersGold.gold." + goldCollector.getPronouns(),
                        user.getDisplayName(server)))
                .appendNewLine();
        List<GoldMessage> messages = goldCollector.getGoldMessages().stream()
                .sorted()
                .collect(Collectors.toList());
        String date = GoldBot.getMessage("userMessage.date");
        String goldCount = GoldBot.getMessage("userMessage.goldCount");
        String link = GoldBot.getMessage("userMessage.link");
        for (int i = 0; i < messages.size(); i++) {
            if (i > 0 && i % 10 == 0) {
                mb.send(event.getChannel());
                mb = new MessageBuilder();
            }
            GoldMessage message = messages.get(i);
            mb = mb.append((i + 1) + ".")
                    .append(String.format(" %s ", date), MessageDecoration.BOLD)
                    .append(formatter.format(message.getMessageTimestamp()))
                    .append(String.format(" %s ", goldCount), MessageDecoration.BOLD).append(message.getMessageGold())
                    .append(String.format(" %s ", link), MessageDecoration.BOLD).append(message.getMessageURL())
                    .appendNewLine();
        }
         mb.send(event.getChannel());
    }
}
