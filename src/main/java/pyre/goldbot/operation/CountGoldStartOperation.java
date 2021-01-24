package pyre.goldbot.operation;

import pyre.goldbot.db.GoldDao;
import pyre.goldbot.db.entity.GoldCollector;

import java.util.List;

public class CountGoldStartOperation extends Operation {

    public CountGoldStartOperation() {
        super(null, "-1", null);
    }

    @Override
    public void execute() {
        List<GoldCollector> goldCollectors = GoldDao.getInstance().getGoldCollectorsWithMessages();
        for (GoldCollector goldCollector : goldCollectors) {
            goldCollector.setGoldCount(0);
            goldCollector.getGoldMessages().clear();
        }
        GoldDao.getInstance().saveOrUpdate(goldCollectors);
    }
}
