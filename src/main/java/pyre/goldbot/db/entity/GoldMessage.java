package pyre.goldbot.db.entity;

import javax.persistence.*;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "gold_messages")
public class GoldMessage implements Comparable<GoldMessage> {

    @Id
    @Column(name = "message_id")
    private String messageId;

    @ManyToOne
    @JoinColumn(name = "gold_collector_id")
    private GoldCollector goldCollector;

    @Column(name = "message_timestamp")
    private Instant messageTimestamp;

    @Column(name = "message_gold")
    private int messageGold;

    @Column(name = "message_url")
    private String messageURL;

    public GoldMessage() {
    }

    public GoldMessage(String messageId, GoldCollector goldCollector, Instant messageTimestamp, int messageGold,
                       String messageURL) {
        this.messageId = messageId;
        this.goldCollector = goldCollector;
        this.messageTimestamp = messageTimestamp;
        this.messageGold = messageGold;
        this.messageURL = messageURL;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public GoldCollector getGoldCollector() {
        return goldCollector;
    }

    public void setGoldCollector(GoldCollector goldCollector) {
        this.goldCollector = goldCollector;
    }

    public Instant getMessageTimestamp() {
        return messageTimestamp;
    }

    public void setMessageTimestamp(Instant messageTimestamp) {
        this.messageTimestamp = messageTimestamp;
    }

    public int getMessageGold() {
        return messageGold;
    }

    public void setMessageGold(int messageGold) {
        this.messageGold = messageGold;
    }

    public String getMessageURL() {
        return messageURL;
    }

    public void setMessageURL(String messageURL) {
        this.messageURL = messageURL;
    }

    public void increaseMessageGold() {
        messageGold++;
    }

    public void decreaseMessageGold() {
        messageGold--;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoldMessage that = (GoldMessage) o;
        return messageId.equals(that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId);
    }

    @Override
    public int compareTo(GoldMessage o) {
        return messageTimestamp.compareTo(o.messageTimestamp);
    }
}
