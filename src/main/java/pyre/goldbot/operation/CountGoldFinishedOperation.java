package pyre.goldbot.operation;

import org.javacord.api.entity.message.Message;
import pyre.goldbot.GoldBot;

public class CountGoldFinishedOperation extends Operation {

    public CountGoldFinishedOperation(Message message) {
        super(null, "-1", message);
    }

    @Override
    public void execute() {
        message.edit(GoldBot.getMessage("countGold.finished"));
    }
}
