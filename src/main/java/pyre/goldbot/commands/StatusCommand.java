package pyre.goldbot.commands;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

public class StatusCommand implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageAuthor().isBotUser()) {
            return;
        }
        if (!event.getMessageContent().equalsIgnoreCase("!status")) {
            return;
        }
        event.getChannel().sendMessage("GoldBot \u017Cyje i ma si\u0119 dobrze!");
    }
}
