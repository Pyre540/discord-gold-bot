package pyre.goldbot.operation;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.Message;
import pyre.goldbot.GoldManager;

import java.time.Instant;
import java.util.Map;

public class CountGoldFinishedOperation extends Operation {

    private Message message;

    public CountGoldFinishedOperation(DiscordApi api, String channelId, Instant msgTimestamp, Message message) {
        super(api, channelId, msgTimestamp);
        this.message = message;
    }

    @Override
    public void execute(Map<String, GoldManager.GoldCollector> goldCollectors) {
        message.edit("Przeliczanie z\u0142ota zako\u0144czone! Aby zobaczy\u0107 aktualny ranking u\u017Cyj komendy" +
                " !ranking.");
    }
}
