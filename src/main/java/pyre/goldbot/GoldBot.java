package pyre.goldbot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.user.UserStatus;

public class GoldBot {

    private static final Logger logger = LogManager.getLogger(GoldBot.class);

    public static void main(String[] args) {
        String token = System.getenv("token");
        if (token == null || token.isEmpty()) {
            logger.error("No bot token");
            return;
        }

        DiscordApi api = new DiscordApiBuilder()
                .setToken(token)
                .login()
                .join();
        api.updateStatus(UserStatus.INVISIBLE);
        // Add a listener which answers with "Pong!" if someone writes "!ping"
        api.addMessageCreateListener(event -> {
            if (event.getMessageContent().equalsIgnoreCase("!ping")) {
                event.getChannel().sendMessage("Pong!");
            }
        });

        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: "
                + api.createBotInvite(Permissions.fromBitmask(402721792)));
    }
}
