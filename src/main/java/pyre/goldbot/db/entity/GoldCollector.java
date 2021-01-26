package pyre.goldbot.db.entity;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "gold_collectors")
public class GoldCollector implements Comparable<GoldCollector> {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "gold_count")
    private int goldCount;

    @Column(name = "pronouns", nullable = false, columnDefinition = "varchar(1) default 'M'")
    @Enumerated(EnumType.STRING)
    private Pronouns pronouns = Pronouns.M;

    @OneToMany(mappedBy = "goldCollector", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<GoldMessage> goldMessages;

    public GoldCollector() {
    }

    public GoldCollector(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getGoldCount() {
        return goldCount;
    }

    public void setGoldCount(int goldCount) {
        this.goldCount = goldCount;
    }

    public Pronouns getPronouns() {
        return pronouns;
    }

    public void setPronouns(Pronouns pronouns) {
        this.pronouns = pronouns;
    }

    public Set<GoldMessage> getGoldMessages() {
        if (goldMessages == null) {
            goldMessages = new HashSet<>();
        }
        return goldMessages;
    }

    public void setGoldMessages(Set<GoldMessage> goldMessages) {
        getGoldMessages().clear();
        this.goldMessages.addAll(goldMessages);
    }

    public Optional<GoldMessage> getGoldMessage(String msgId) {
        return goldMessages.stream()
                .filter(m -> m.getMessageId().equals(msgId))
                .findFirst();
    }

    public void addGoldMessage(GoldMessage message) {
        if (goldMessages == null) {
            goldMessages = new HashSet<>();
        }
        goldMessages.add(message);
    }

    public void removeGoldMessage(GoldMessage message) {
        goldMessages.removeIf(m -> m.getMessageId().equals(message.getMessageId()));
    }

    public void modifyGoldCount(int count) {
        this.goldCount += count;
    }

    public void increaseGoldCount() {
        this.goldCount++;
    }

    public void decreaseGoldCount() {
        this.goldCount--;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoldCollector that = (GoldCollector) o;
        return goldCount == that.goldCount && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, goldCount);
    }

    @Override
    public int compareTo(GoldCollector o) {
        return Comparator.comparing(GoldCollector::getGoldCount)
                .thenComparing(GoldCollector::getUserId)
                .compare(this, o);
    }

    public enum Pronouns {
        M, F
    }
}
