package pyre.goldbot.operation;

import org.javacord.api.entity.message.Message;
import pyre.goldbot.entity.GoldCollector;

import java.time.Instant;
import java.util.Map;

public class CountGoldFinishedOperation extends Operation {

    private Message message;

    public CountGoldFinishedOperation(Message message) {
        super(null, "-1", Instant.MAX);
        this.message = message;
    }

    @Override
    public void execute(Map<String, GoldCollector> goldCollectors) {
        message.edit("Przeliczanie z\u0142ota zako\u0144czone! Aby zobaczy\u0107 aktualny ranking u\u017Cyj komendy" +
                " !ranking.");
    }
}
