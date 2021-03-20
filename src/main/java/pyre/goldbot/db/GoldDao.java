package pyre.goldbot.db;

import org.hibernate.Session;
import org.hibernate.query.Query;
import pyre.goldbot.db.entity.GoldCollector;
import pyre.goldbot.db.entity.GoldMessage;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class GoldDao {

    private static final GoldDao INSTANCE = new GoldDao();

    private GoldDao() {
    }

    public static GoldDao getInstance() {
        return INSTANCE;
    }

    public void saveOrUpdate(GoldCollector goldCollector) {
        TransactionUtil.doTransaction(() -> HibernateUtil.getSession().saveOrUpdate(goldCollector));
    }

    public void saveOrUpdate(Collection<GoldCollector> goldCollectors) {
        TransactionUtil.doTransaction(() -> {
            Session session = HibernateUtil.getSession();
            for (GoldCollector goldCollector : goldCollectors) {
                session.saveOrUpdate(goldCollector);
            }
        });
    }

    public GoldCollector getGoldCollector(String userId) {
        return TransactionUtil.doTransaction(() -> {
            Session session = HibernateUtil.getSession();
            return session.createQuery("select c from GoldCollector c where c.id = :userId", GoldCollector.class)
                    .setParameter("userId", userId)
                    .uniqueResult();
        });
    }

    public GoldCollector getGoldCollectorWithMessages(String userId) {
        return TransactionUtil.doTransaction(() -> {
            Session session = HibernateUtil.getSession();
            return session.createQuery("select c from GoldCollector c left join fetch c.goldMessages where c.id = " +
                    ":userId", GoldCollector.class)
                    .setParameter("userId", userId)
                    .uniqueResult();
        });
    }

    public List<GoldCollector> getGoldCollectors() {
        return TransactionUtil.doTransaction(() -> {
            Session session = HibernateUtil.getSession();
            Query<GoldCollector> query = session.createQuery("select c from GoldCollector c", GoldCollector.class);
            return query.getResultList();
        });
    }

    public List<GoldCollector> getGoldCollectorsWithMessages() {
        return TransactionUtil.doTransaction(() -> {
            Session session = HibernateUtil.getSession();
            Query<GoldCollector> query = session.createQuery("select distinct c from GoldCollector c " +
                    "left join fetch c.goldMessages", GoldCollector.class);
            return query.getResultList();
        });
    }

    public Map<String, GoldCollector> getGoldCollectorsWithMessagesAsMap() {
        return getGoldCollectorsWithMessages().stream()
                .collect(Collectors.toMap(GoldCollector::getUserId, c -> c));
    }

    public void updateGoldCollectors(Map<String, GoldCollector> newCollectors) {
        TransactionUtil.doTransaction(() -> {
            List<GoldCollector> goldCollectors = getGoldCollectors();
            for (GoldCollector goldCollector : goldCollectors) {
                if (newCollectors.containsKey(goldCollector.getUserId())) {
                    goldCollector.setGoldCount(newCollectors.get(goldCollector.getUserId()).getGoldCount());
                    updateMessages(goldCollector, newCollectors.get(goldCollector.getUserId()).getGoldMessages());
                    newCollectors.remove(goldCollector.getUserId());
                } else {
                    goldCollector.setGoldCount(0);
                    goldCollector.getGoldMessages().clear();
                }
            }
            saveOrUpdate(goldCollectors);
            if (!newCollectors.isEmpty()) {
                saveOrUpdate(newCollectors.values());
            }
        });
    }

    private void updateMessages(GoldCollector goldCollector, Set<GoldMessage> newMessages) {
        List<GoldMessage> toRemove = new ArrayList<>();
        Map<String, GoldMessage> newMessagesMap = newMessages.stream()
                .collect(Collectors.toMap(GoldMessage::getMessageId, m -> m));
        Set<GoldMessage> currentMessages = goldCollector.getGoldMessages();
        for (GoldMessage currentMessage : currentMessages) {
            if (newMessagesMap.containsKey(currentMessage.getMessageId())) {
                currentMessage.setMessageGold(newMessagesMap.get(currentMessage.getMessageId()).getMessageGold());
                newMessagesMap.remove(currentMessage.getMessageId());
            } else {
                toRemove.add(currentMessage);
            }
        }
        toRemove.forEach(goldCollector::removeGoldMessage);
        newMessagesMap.values().forEach(goldCollector::addGoldMessage);
    }

    public List<GoldMessage> getGoldMessages(String userId) {
        return TransactionUtil.doTransaction(() -> {
            Session session = HibernateUtil.getSession();
            Query<GoldMessage> query = session.createQuery("select m from GoldMessage m " +
                    "where m.goldCollector.userId = :userId", GoldMessage.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        });
    }

    public List<GoldCollector> getLeaders() {
        return TransactionUtil.doTransaction(() -> {
            Session session = HibernateUtil.getSession();
            Query<GoldCollector> query = session.createQuery("select c from GoldCollector c " +
                    "where c.goldCount = (select max(b.goldCount) from GoldCollector b)", GoldCollector.class);
            return query.getResultList();
        });
    }

    public List<GoldMessage> getLastWeekGoldMessages() {
        return TransactionUtil.doTransaction(() -> {
            Instant date = Instant.now().minus(7, ChronoUnit.DAYS);
            Session session = HibernateUtil.getSession();
            Query<GoldMessage> query = session.createQuery("select m from GoldMessage m " +
                    "where m.messageTimestamp >= :date", GoldMessage.class)
                    .setParameter("date", date);
            return query.getResultList();
        });
    }
}
