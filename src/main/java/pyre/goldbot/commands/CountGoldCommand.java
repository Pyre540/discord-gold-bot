package pyre.goldbot.commands;

import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import pyre.goldbot.GoldBot;
import pyre.goldbot.GoldManager;
import pyre.goldbot.operation.CountGoldFinishedOperation;
import pyre.goldbot.operation.CountGoldOperation;
import pyre.goldbot.operation.CountGoldStartOperation;
import pyre.goldbot.operation.Operation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CountGoldCommand implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageAuthor().isBotUser()) {
            return;
        }
        if (!event.getMessageContent().equalsIgnoreCase("!countGold")) {
            return;
        }
        countGold(event);
    }

    private void countGold(MessageCreateEvent event) {
        Message msg = event.getChannel().sendMessage(GoldBot.getMessage("countGold.counting")).join();
        DiscordApi api = event.getApi();
        Collection<ServerTextChannel> textChannels = api.getServerTextChannels();
        int i = 1;
        List<Operation> operations = new ArrayList<>();
        operations.add(new CountGoldStartOperation());
        for (ServerTextChannel textChannel : textChannels) {
            operations.add(new CountGoldOperation(api, textChannel.getIdAsString(), i, textChannels.size(), msg));
            i++;
        }
        operations.add(new CountGoldFinishedOperation(msg));
        boolean addedOps = GoldManager.getInstance().addOperations(operations);
        if (!addedOps) {
            msg.edit(GoldBot.getMessage("countGold.alreadyRunning"));
        }
    }
}
