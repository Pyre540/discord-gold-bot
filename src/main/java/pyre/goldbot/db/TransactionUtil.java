package pyre.goldbot.db;

public final class TransactionUtil {

    private TransactionUtil() {
        // do not instantiate
    }

    @FunctionalInterface
    public static interface TransactionWithoutResult {
        void perform();
    }

    @FunctionalInterface
    public static interface TransactionWithResult<R> {
        R perform();
    }

    public static void doTransaction(TransactionWithoutResult transaction) {
        try {
            HibernateUtil.beginTransaction();

            transaction.perform();

            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction(e);
        }
    }

    public static <R> R doTransaction(TransactionWithResult<R> transaction) {
        R result = null;
        try {
            HibernateUtil.beginTransaction();

            result = transaction.perform();

            HibernateUtil.commitTransaction();
            HibernateUtil.closeSession();
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction(e);
        }
        return result;
    }
}
