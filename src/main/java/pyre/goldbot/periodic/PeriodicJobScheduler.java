package pyre.goldbot.periodic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.TimeZone;

public class PeriodicJobScheduler {

    private static final Logger logger = LogManager.getLogger(PeriodicJobScheduler.class);

    private static Scheduler scheduler;

    private PeriodicJobScheduler() {
        //hide constructor
    }

    public static void init() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();

        JobDetail weeklyMostGold = JobBuilder.newJob(WeeklyMostGold.class)
                .withIdentity("Weekly Most Gold")
                .build();
        CronTrigger weeklyGoldTrigger = TriggerBuilder.newTrigger()
                .withIdentity("weeklyGoldTrigger")
                .withSchedule(CronScheduleBuilder.weeklyOnDayAndHourAndMinute(DateBuilder.SUNDAY, 18, 0)
                        .inTimeZone(TimeZone.getTimeZone("Europe/Warsaw")))
                .build();

        scheduler.scheduleJob(weeklyMostGold, weeklyGoldTrigger);
        scheduler.start();
    }

    public static void shutdown() {
        try {
            if (scheduler != null && !scheduler.isShutdown()) {
                scheduler.shutdown();
            }
        } catch (SchedulerException e) {
            logger.error("Cannot shutdown PeriodicJobScheduler.", e);
        }
    }
}
