package pyre.goldbot.entity;

import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class GoldCollector implements Comparable<GoldCollector> {
    private String userId;
    private int score;
    private Set<URL> goldMessages;

    public GoldCollector(String userId) {
        this.userId = userId;
        this.score = 0;
        this.goldMessages = new LinkedHashSet<>();
    }

    public String getUserId() {
        return userId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void modifyScore(int count) {
        this.score += count;
    }

    public void increaseScore() {
        this.score++;
    }

    public void decreaseScore() {
        this.score--;
    }

    public Set<URL> getGoldMessages() {
        return goldMessages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoldCollector that = (GoldCollector) o;
        return score == that.score && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, score);
    }

    @Override
    public int compareTo(GoldCollector o) {
        return getScore() - o.getScore();
    }
}
