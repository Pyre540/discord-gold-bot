package pyre.goldbot.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import pyre.goldbot.db.entity.DbGoldCollector;
import pyre.goldbot.db.entity.DbGoldMessage;

public class GoldDao {

    private static final Logger logger = LogManager.getLogger(GoldDao.class);

    private static final GoldDao INSTANCE = new GoldDao();

    private static SessionFactory sessionFactory;

    private GoldDao() {
    }

    public static boolean init() {
        if (sessionFactory != null) {
            return true;
        }
        Configuration config = new Configuration();
        config.configure();
        config.addAnnotatedClass(DbGoldCollector.class);
        config.addAnnotatedClass(DbGoldMessage.class);

        String jdbcDbUrl = System.getenv("JDBC_DATABASE_URL");
        if (jdbcDbUrl == null || jdbcDbUrl.isEmpty()) {
            logger.error("No JDBC URL!");
            return false;
        }
        config.setProperty("hibernate.connection.url", System.getenv("JDBC_DATABASE_URL"));

        ServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySettings(config.getProperties())
                .build();

        sessionFactory = config.buildSessionFactory(registry);
        logger.info("Successfully connected to database!");

        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.beginTransaction();
        currentSession.save(new DbGoldCollector("1234"));
        currentSession.getTransaction().commit();
        return true;
    }
}
