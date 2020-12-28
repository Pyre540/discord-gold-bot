package pyre.goldbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
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

public class GoldBot {

    private static final Logger logger = LogManager.getLogger(GoldBot.class);

    public static KnownCustomEmoji GOLD_EMOJI;

    public static void main(String[] args) {
        String token = System.getenv("token");
        if (token == null || token.isEmpty()) {
            logger.error("No bot token");
            return;
        }

        DiscordApi api = new DiscordApiBuilder()
                .setToken(token)
                .setAllIntentsExcept(Intent.GUILD_PRESENCES)
                .login()
                .join();
        api.updateStatus(UserStatus.INVISIBLE);

        GOLD_EMOJI = api.getCustomEmojiById(769253894517555240L).orElse(null);
        if (GOLD_EMOJI == null) {
            logger.error("Cannot load gold emoji");
            api.disconnect();
            return;
        }

        api.addListener(new HelpCommand());
        api.addListener(new StatusCommand());
        api.addListener(new CountGoldCommand());
        api.addListener(new RankingCommand());
        api.addListener(new AddGoldReactionListener());
        api.addListener(new RemoveGoldReactionListener());

        GoldManager.getInstance().initRanking(api);

        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: "
                + api.createBotInvite(Permissions.fromBitmask(402721792)));
    }
}
