package pyre.goldbot.db.entity;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "gold_collectors")
public class DbGoldCollector implements Comparable<DbGoldCollector> {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "gold_count")
    private int goldCount;

    @Column(name = "gender")
    private Gender gender;

    @OneToMany(mappedBy = "messageId")
    private List<DbGoldMessage> goldMessages;

    public DbGoldCollector() {
    }

    public DbGoldCollector(String userId) {
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

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public List<DbGoldMessage> getGoldMessages() {
        return goldMessages;
    }

    public void setGoldMessages(List<DbGoldMessage> goldMessages) {
        this.goldMessages = goldMessages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DbGoldCollector that = (DbGoldCollector) o;
        return goldCount == that.goldCount && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, goldCount);
    }

    @Override
    public int compareTo(DbGoldCollector o) {
        return getGoldCount() - o.getGoldCount();
    }

    public enum Gender {
        M, F
    }
}
