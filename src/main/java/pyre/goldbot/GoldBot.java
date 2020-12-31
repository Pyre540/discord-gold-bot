package pyre.goldbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.emoji.KnownCustomEmoji;
import org.javacord.api.entity.intent.Intent;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.user.UserStatus;
import pyre.goldbot.commands.CountGoldCommand;
import pyre.goldbot.commands.HelpCommand;
import pyre.goldbot.commands.RankingCommand;
import pyre.goldbot.commands.StatusCommand;
import pyre.goldbot.listeners.AddGoldReactionListener;
import pyre.goldbot.listeners.RemoveGoldReactionListener;

import java.util.Locale;
import java.util.ResourceBundle;

public class GoldBot {

    private static final Logger logger = LogManager.getLogger(GoldBot.class);

    private static ResourceBundle messages;

    private static DiscordApi api;
    private static KnownCustomEmoji goldEmoji;
    private static TextChannel mainChannel;

    public static void main(String[] args) {
        String token = System.getenv("token");
        if (token == null || token.isEmpty()) {
            logger.error("No bot token");
            return;
        }

        Locale locale = new Locale("pl", "PL");
        messages = ResourceBundle.getBundle("Messages", locale);

        api = new DiscordApiBuilder()
                .setToken(token)
                .setAllIntentsExcept(Intent.GUILD_PRESENCES)
                .login()
                .join();
        api.updateStatus(UserStatus.INVISIBLE);

        goldEmoji = api.getCustomEmojiById(769253894517555240L).orElse(null);
        if (goldEmoji == null) {
            logger.error("Cannot load gold emoji");
            api.disconnect();
            return;
        }

        mainChannel = api.getTextChannelById(792049029679808534L).orElse(null); //todo change id
        if (mainChannel == null) {
            logger.error("Cannot load main text channel");
            api.disconnect();
            return;
        }

        api.addListener(new HelpCommand());
        api.addListener(new StatusCommand());
        api.addListener(new CountGoldCommand());
        api.addListener(new RankingCommand());
        api.addListener(new AddGoldReactionListener());
        api.addListener(new RemoveGoldReactionListener());

        GoldManager.getInstance().initRanking();

        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: "
                + api.createBotInvite(Permissions.fromBitmask(402721792)));
    }

    public static ResourceBundle getMessages() {
        return messages;
    }

    public static DiscordApi getApi() {
        return api;
    }

    public static KnownCustomEmoji getGoldEmoji() {
        return goldEmoji;
    }

    public static TextChannel getMainChannel() {
        return mainChannel;
    }
}
