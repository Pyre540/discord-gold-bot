package pyre.goldbot.commands;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import pyre.goldbot.GoldBot;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class StatusCommand implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageAuthor().isBotUser()) {
            return;
        }
        if (!event.getMessageContent().equalsIgnoreCase("!status")) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tempDateTime = LocalDateTime.from(GoldBot.BOT_START);

        long years = ChronoUnit.YEARS.between(tempDateTime, now);
        tempDateTime = tempDateTime.plusYears(years);

        long months = ChronoUnit.MONTHS.between(tempDateTime, now);
        tempDateTime = tempDateTime.plusMonths(months);

        long days = ChronoUnit.DAYS.between(tempDateTime, now);
        tempDateTime = tempDateTime.plusDays(days);

        long hours = ChronoUnit.HOURS.between(tempDateTime, now);
        tempDateTime = tempDateTime.plusHours(hours);

        long minutes = ChronoUnit.MINUTES.between(tempDateTime, now);
        tempDateTime = tempDateTime.plusMinutes(minutes);

        long seconds = ChronoUnit.SECONDS.between(tempDateTime, now);

        event.getChannel().sendMessage(String.format(GoldBot.getMessages().getString("status.message"), years, months,
                days, hours, minutes, seconds));
    }
}
