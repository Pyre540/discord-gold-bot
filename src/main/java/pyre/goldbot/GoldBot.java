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
import pyre.goldbot.commands.*;
import pyre.goldbot.listeners.AddGoldReactionListener;
import pyre.goldbot.listeners.RemoveGoldReactionListener;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;

public class GoldBot {

    private static final Logger logger = LogManager.getLogger(GoldBot.class);

    private static ResourceBundle messages;

    private static final Properties config = new Properties();

    public static final LocalDateTime BOT_START = LocalDateTime.now();

    public static final String CROWN = "\uD83D\uDC51";

    private static DiscordApi api;
    private static KnownCustomEmoji goldEmoji;
    private static TextChannel mainChannel;

    public static void main(String[] args) throws IOException {
        String token = System.getenv("token");
        if (token == null || token.isEmpty()) {
            logger.error("No bot token");
            return;
        }

        Locale locale = new Locale("pl", "PL");
        messages = ResourceBundle.getBundle("Messages", locale);

        try (InputStream input = GoldBot.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                logger.error("Unable to find config.properties");
                return;
            }
            config.load(input);
        }

        api = new DiscordApiBuilder()
                .setToken(token)
                .setAllIntentsExcept(Intent.GUILD_PRESENCES)
                .login()
                .join();
        api.updateStatus(UserStatus.INVISIBLE);

        goldEmoji = api.getCustomEmojiById(config.getProperty("goldEmojiId")).orElse(null);
        if (goldEmoji == null) {
            logger.error("Cannot load gold emoji");
            api.disconnect();
            return;
        }

        mainChannel = api.getTextChannelById(config.getProperty("mainChannelId")).orElse(null);
        if (mainChannel == null) {
            logger.error("Cannot load main text channel");
            api.disconnect();
            return;
        }

        api.addListener(new HelpCommand());
        api.addListener(new StatusCommand());
        api.addListener(new CountGoldCommand());
        api.addListener(new RankingCommand());
        api.addListener(new UsersGoldCommand());
        api.addListener(new AddGoldReactionListener());
        api.addListener(new RemoveGoldReactionListener());

        GoldManager.getInstance().initRanking();

        // Print the invite url of your bot
        logger.info("You can invite the bot by using the following url: {}",
                api.createBotInvite(Permissions.fromBitmask(402721792)));
    }

    public static String getMessage(String key) {
        return messages.getString(key);
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
