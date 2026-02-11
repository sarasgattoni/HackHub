package it.hackhub.model.utils;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateExecutor {

    /**
     * Executes a Hibernate function without a transaction.
     * The session is opened and closed automatically.
     *
     * @param <R>      the type of the result
     * @param function the Hibernate function to execute
     * @return the result of the function
     * @throws RuntimeException if the function throws any runtime exception
     */
    public static <R> R execute(HibernateFunction<R> function) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return function.apply(session);
        } catch (RuntimeException ex) {
            throw ex;
        }
    }

    /**
     * Executes a Hibernate function within a transaction.
     * The session is opened and closed automatically, and the transaction
     * is committed if no exceptions occur.
     *
     * @param <R>      the type of the result
     * @param function the Hibernate function to execute
     * @return the result of the function
     * @throws RuntimeException if the function throws any runtime exception
     */
    public static <R> R executeTransaction(HibernateFunction<R> function) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            R result = function.apply(session);
            tx.commit();
            return result;
        } catch (RuntimeException ex) {
            throw ex;
        }
    }

    /**
     * Executes a void Hibernate consumer within a transaction.
     * The session is opened and closed automatically, and the transaction
     * is committed if no exceptions occur.
     *
     * @param consumer the Hibernate consumer to execute
     * @throws RuntimeException if the consumer throws any runtime exception
     */
    public static void executeVoidTransaction(HibernateConsumer consumer) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            consumer.accept(session);
            tx.commit();
        } catch (RuntimeException ex) {
            throw ex;
        }
    }

    /**
     * Functional interface representing an operation that accepts a Hibernate
     * {@link Session} and returns no result.
     */
    @FunctionalInterface
    public interface HibernateConsumer {
        void accept(Session session);
    }

    /**
     * Functional interface representing an operation that accepts a Hibernate
     * {@link Session} and produces a result.
     *
     * @param <R> the type of the result
     */
    @FunctionalInterface
    public interface HibernateFunction<R> {
        R apply(Session session);

    }
}