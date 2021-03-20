package pyre.goldbot.periodic;

import org.javacord.api.entity.message.MessageBuilder;
import org.javacord.api.entity.message.MessageDecoration;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import pyre.goldbot.GoldBot;
import pyre.goldbot.db.GoldDao;
import pyre.goldbot.db.entity.GoldCollector;
import pyre.goldbot.db.entity.GoldMessage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WeeklyMostGold implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM");
        LocalDate today = LocalDate.now();
        String goldEmote = GoldBot.getGoldEmoji().getMentionTag();

        MessageBuilder mb = new MessageBuilder()
                .append(GoldBot.getMessage("weeklyMostGold.header", goldEmote, today.minusWeeks(1).format(formatter),
                        today.format(formatter), goldEmote), MessageDecoration.BOLD)
                .appendNewLine();

        List<GoldMessage> lastWeekGoldMessages = GoldDao.getInstance().getLastWeekGoldMessages();

        if (lastWeekGoldMessages.isEmpty()) {
            mb.append(GoldBot.getMessage("weeklyMostGold.noGold"));
        } else {
            Map<String, UsersGold> usersGold = new HashMap<>();
            lastWeekGoldMessages.forEach(goldMessage -> {
                UsersGold userGold = usersGold.computeIfAbsent(goldMessage.getGoldCollector().getUserId(),
                        i -> new UsersGold(goldMessage.getGoldCollector()));
                userGold.sumGold += goldMessage.getMessageGold();
            });

            Server server = GoldBot.getApi().getServers().iterator().next();
            usersGold.values().stream()
                    .sorted(Comparator.comparingInt(UsersGold::getSumGold).reversed())
                    .forEach(u -> {
                        User user = GoldBot.getApi().getUserById(u.collector.getUserId()).join();
                        String msg = GoldBot.getMessage("weeklyMostGold.record", user.getDisplayName(server), u.sumGold,
                                goldEmote, u.collector.getGoldCount(), goldEmote);
                        mb.append(msg).appendNewLine();
                    });
            mb.append(GoldBot.getMessage("weeklyMostGold.congrats"));
        }
        mb.send(GoldBot.getMainChannel());
    }

    private class UsersGold {
        private GoldCollector collector;
        private int sumGold = 0;

        public UsersGold(GoldCollector collector) {
            this.collector = collector;
        }

        public int getSumGold() {
            return sumGold;
        }
    }
}
