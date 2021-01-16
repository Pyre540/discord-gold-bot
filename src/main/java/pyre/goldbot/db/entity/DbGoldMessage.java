package pyre.goldbot.db.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gold_messages")
public class DbGoldMessage {

    @Id
    @Column(name = "message_id")
    private String messageId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name = "gold_collector_id")
    private DbGoldCollector goldCollector;

    @Column(name = "message_timestamp")
    private LocalDateTime messageTimestamp;

    @Column(name = "message_gold")
    private int messageGold;

    @Column(name = "message_url")
    private String messageURL;

    public DbGoldMessage() {
    }

    public DbGoldMessage(String messageId, DbGoldCollector goldCollector, LocalDateTime messageTimestamp,
                         int messageGold, String messageURL) {
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

    public DbGoldCollector getGoldCollector() {
        return goldCollector;
    }

    public void setGoldCollector(DbGoldCollector goldCollector) {
        this.goldCollector = goldCollector;
    }

    public LocalDateTime getMessageTimestamp() {
        return messageTimestamp;
    }

    public void setMessageTimestamp(LocalDateTime messageTimestamp) {
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
}
