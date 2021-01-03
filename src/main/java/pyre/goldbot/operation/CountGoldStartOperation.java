package pyre.goldbot.operation;

import pyre.goldbot.entity.GoldCollector;

import java.time.Instant;
import java.util.Map;

public class CountGoldStartOperation extends Operation {

    public CountGoldStartOperation() {
        super(null, "-1", Instant.MAX);
    }

    @Override
    public void execute(Map<String, GoldCollector> goldCollectors) {
        for (GoldCollector goldCollector : goldCollectors.values()) {
            goldCollector.setScore(0);
        }
    }
}
