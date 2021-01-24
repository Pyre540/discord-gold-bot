package pyre.goldbot.db;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.service.ServiceRegistry;
import pyre.goldbot.db.entity.GoldCollector;
import pyre.goldbot.db.entity.GoldMessage;

public class HibernateUtil {

    private static final Logger logger = LogManager.getLogger(HibernateUtil.class);

    private static SessionFactory sessionFactory;

    /** a Session object for the thread */
    private static final ThreadLocal<Session> threadSession = new ThreadLocal<>();

    /** a Transaction object for the thread */
    private static final ThreadLocal<Transaction> threadTransaction = new ThreadLocal<>();

    /** a Transaction level for the thread */
    private static final ThreadLocal<Integer> threadTransactionLevel = new ThreadLocal<>();

    private HibernateUtil(){
        // do not instantiate
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory != null) {
            return sessionFactory;
        }
        Configuration config = new Configuration();
        config.configure();
        config.addAnnotatedClass(GoldCollector.class);
        config.addAnnotatedClass(GoldMessage.class);

        String jdbcDbUrl = System.getenv("JDBC_DATABASE_URL");
        if (jdbcDbUrl == null || jdbcDbUrl.isEmpty()) {
            throw new RuntimeException("No JDBC URL!");
        }
        config.setProperty("hibernate.connection.url", System.getenv("JDBC_DATABASE_URL"));

        ServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySettings(config.getProperties())
                .build();

        sessionFactory = config.buildSessionFactory(registry);
        logger.info("Successfully connected to database!");

        return sessionFactory;
    }

    public static Session getSession() {
        Session session = threadSession.get();
        if (session == null) {
            logger.debug("Opening new Session for thread {}.", Thread.currentThread().getName());
            session = getSessionFactory().openSession();
            threadSession.set(session);
        }
        return session;
    }

    public static void closeSession() {
        if (getTransactionLevel() > 0) {
            logger.debug("There are open transactions for thread {}. Transaction level is {}.",
                    Thread.currentThread().getName(), getTransactionLevel());
            return;
        }
        Session session = threadSession.get();
        threadSession.remove();
        if (session != null && session.isOpen()) {
            logger.debug("Closing Session of thread {}.", Thread.currentThread().getName());
            session.close();
        }
    }

    public static void beginTransaction() {
        Transaction tx = threadTransaction.get();
        if (tx == null) {
            logger.debug("Starting new database transaction in {} thread.", Thread.currentThread().getName());
            tx = getSession().beginTransaction();
            threadTransaction.set(tx);
            threadTransactionLevel.set(1);
        } else {
            levelUp();
        }
    }

    /**
     * Commit the database transaction.
     */
    public static void commitTransaction() {
        if (getTransactionLevel() > 1) {
            levelDown();
            return;
        }

        Transaction tx = threadTransaction.get();
        try {
            if (tx != null && tx.getStatus().isNotOneOf(TransactionStatus.COMMITTED, TransactionStatus.COMMITTING,
                    TransactionStatus.ROLLED_BACK, TransactionStatus.ROLLING_BACK)) {
                logger.debug("Committing database transaction of thread {}.", Thread.currentThread().getName());
                tx.commit();
            }
            threadTransaction.remove();
            threadTransactionLevel.remove();
        } catch (HibernateException e) {
            rollbackTransaction(e);
        }
    }

    /**
     * Returns level of transaction. Level 1 means that this is a top level
     * transaction. Any level greater than 1 means that another manager/service
     * begun a new transaction in the same thread. To decrease transaction level
     * commitTransaction or rollbackTransaction must be called.
     * @return Level of transaction
     */
    public static synchronized int getTransactionLevel() {
        int level = 0;
        if (threadTransaction.get() != null && threadTransactionLevel.get() != null) {
            level = threadTransactionLevel.get();
        }
        return level;
    }

    /**
     * Increases the current transaction level by one.
     */
    private static synchronized void levelUp() {
        Integer level = threadTransactionLevel.get();
        if (level != null) {
            int value = level;
            threadTransactionLevel.set(++value);
            logger.debug("INCREASING transaction level to {} for thread {}.", value, Thread.currentThread().getName());
        }
    }

    /**
     * Decreases the current transaction level by one.
     */
    private static synchronized void levelDown() {
        Integer level = threadTransactionLevel.get();
        if (level != null && level > 1) {
            int value = level;
            threadTransactionLevel.set(--value);
            logger.debug("DECREASING transaction level to {} for thread {}.", value, Thread.currentThread().getName());
        }
    }

    /**
     * Rollback the database transaction.
     * @param e - exception that caused rolllback
     */
    public static void rollbackTransaction(Exception e) {
        if (getTransactionLevel() > 1) {
            levelDown();
            logAndRethrow(e);
        }

        Transaction tx = threadTransaction.get();
        try {
            threadTransaction.remove();
            threadTransactionLevel.remove();
            if (tx != null && tx.getStatus().isNotOneOf(TransactionStatus.COMMITTED, TransactionStatus.COMMITTING,
                    TransactionStatus.ROLLED_BACK, TransactionStatus.ROLLING_BACK)) {
                logger.debug("Trying to rollback database transaction of thread {}.", Thread.currentThread().getName());
                tx.rollback();
            }
            closeSession();
        } catch (HibernateException e1) {
            logger.error("Error rollbacking transaction", e1);
        }
        logAndRethrow(e);
    }

    private static void logAndRethrow(Exception e) {
        if (e instanceof HibernateException) {
            logger.error("Database exception occurred. Rollbacking transaction!", e);
            throw new RuntimeException("Database exception occurred: " + e.getMessage());
        } else {
            logger.error("Unhandled exception occurred. Rollbacking transaction!", e);
            boolean isStandard = e.getClass().getName().startsWith("java.lang.");
            throw new RuntimeException("Unhandled exception occurred: " + e.getMessage(), isStandard ? e : null);
        }
    }
}
