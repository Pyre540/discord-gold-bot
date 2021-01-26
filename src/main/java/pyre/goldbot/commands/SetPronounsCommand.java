package pyre.goldbot.commands;

import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;
import pyre.goldbot.GoldBot;
import pyre.goldbot.db.GoldDao;
import pyre.goldbot.db.entity.GoldCollector;

public class SetPronounsCommand implements MessageCreateListener {

    @Override
    public void onMessageCreate(MessageCreateEvent event) {
        if (event.getMessageAuthor().isBotUser()) {
            return;
        }
        if (!event.getMessageContent().matches("^!setPronouns \\w$")) {
            return;
        }
        String pronouns = event.getMessageContent().substring(event.getMessageContent().indexOf(' ')).trim();
        String userId = event.getMessageAuthor().getIdAsString();
        GoldCollector goldCollector = GoldDao.getInstance().getGoldCollector(userId);
        if (goldCollector == null) {
            goldCollector = new GoldCollector(userId);
        }

        GoldCollector.Pronouns chosenPronouns = getPronouns(pronouns);
        if (chosenPronouns == null) {
            event.getMessageAuthor().asUser().ifPresent(u ->
                    u.openPrivateChannel().join()
                            .sendMessage(GoldBot.getMessage("setPronouns.set.error")));
            return;
        }

        goldCollector.setPronouns(chosenPronouns);
        GoldDao.getInstance().saveOrUpdate(goldCollector);

        event.getMessageAuthor().asUser().ifPresent(u ->
                u.openPrivateChannel().join()
                        .sendMessage(GoldBot.getMessage("setPronouns.set." + chosenPronouns)));
    }

    private GoldCollector.Pronouns getPronouns(String pronounsString) {
        try {
            return GoldCollector.Pronouns.valueOf(pronounsString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
