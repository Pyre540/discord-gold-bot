package pyre.goldbot.commands;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class HelpCommand implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageAuthor().isBotUser()) {
            return;
        }
        if (!event.getMessageContent().equalsIgnoreCase("!help")) {
            return;
        }
        CompletableFuture<User> author = event.getApi().getUserById(268098638624981007L);
        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("GoldBot")
                .setDescription("Lista dost\u0119pnych komend dla GoldBota")
                .setColor(Color.ORANGE)
                .setThumbnail("https://static.wikia.nocookie.net/minecraft_gamepedia/images/8/8a/Gold_Ingot_JE4_BE2" +
                        ".png/revision/latest")
                .addInlineField("!help", "Wy\u015Bwietla list\u0119 dost\u0119pnych komend.")
                .addInlineField("!status", "Wy\u015Bwietla status GoldBota.")
                .addInlineField("!countGold", "Przelicza na nowo ca\u0142e z\u0142oto na podstawie ca\u0142ej " +
                        "historii wszystkich kana\u0142\u00F3w tekstowych. To troch\u0119 zajmie...")
                .addInlineField("!ranking", "Wy\u015Bwietla aktualny ranking.");
        embed.setAuthor(author.join());
        event.getChannel().sendMessage(embed);
    }
}
