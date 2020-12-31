package pyre.goldbot.commands;

import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import pyre.goldbot.GoldBot;

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
                .setTitle(GoldBot.getMessages().getString("help.title"))
                .setDescription(GoldBot.getMessages().getString("help.description"))
                .setColor(Color.ORANGE)
                .setThumbnail("https://static.wikia.nocookie.net/minecraft_gamepedia/images/8/8a/Gold_Ingot_JE4_BE2" +
                        ".png/revision/latest")
                .addInlineField("!help", GoldBot.getMessages().getString("help.helpCmd"))
                .addInlineField("!status", GoldBot.getMessages().getString("help.statusCmd"))
                .addInlineField("!countGold", GoldBot.getMessages().getString("help.countCmd"))
                .addInlineField("!ranking", GoldBot.getMessages().getString("help.rankingCmd"))
                .addInlineField("!usersGold <username>", GoldBot.getMessages().getString("help.usersGold"));
        embed.setAuthor(author.join());
        event.getChannel().sendMessage(embed);
    }
}
