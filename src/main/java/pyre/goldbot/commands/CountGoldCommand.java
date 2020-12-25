package pyre.goldbot.commands;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CountGoldCommand implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageAuthor().isBotUser()) {
            return;
        }
        if (!event.getMessageContent().equalsIgnoreCase("!countGold")) {
            return;
        }
        event.getApi().getThreadPool().getExecutorService().execute(() -> countGold(event));
    }

    private void countGold(MessageCreateEvent event) {
        Message msg = event.getChannel().sendMessage("Przeliczam z\u0142oto...").join();
        KnownCustomEmoji goldEmoji = event.getApi().getCustomEmojiById(769253894517555240L).orElse(null);
        Collection<ServerTextChannel> textChannels = event.getApi().getServerTextChannels();
        Map<String, Integer> usersGold = new HashMap<>();
        int i = 1;
        for (ServerTextChannel channel : textChannels) {
            msg.edit(String.format("Przeliczam z\u0142oto [%d/%d]...", i, textChannels.size()));
            List<Message> goldMessages = channel.getMessagesAsStream()
                    .filter(m -> m.getReactionByEmoji(goldEmoji).isPresent())
                    .collect(Collectors.toList());
            for (Message message : goldMessages) {
                message.getReactionByEmoji(goldEmoji).ifPresent(r -> {
                    Integer j = usersGold.computeIfAbsent(message.getAuthor().getName(), a -> 0);
                    usersGold.put(message.getAuthor().getName(), j + r.getCount());
                });
            }
            i++;
        }
        msg.edit("Przeliczanie z\u0142ota zako\u0144czone! " + usersGold);
    }
}
