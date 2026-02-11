package it.hackhub.model.utils;

import lombok.Getter;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Utility class for managing the Hibernate {@link SessionFactory} lifecycle.
 * <p>
 * This class builds a singleton {@code SessionFactory} instance using the Hibernate
 * configuration file and provides methods to access and shut down the factory.
 * </p>
 */
public class HibernateUtil {
    /**
     * The singleton {@code SessionFactory} instance, initialized at class loading.
     */
    @Getter
    private static final SessionFactory sessionFactory = buildSessionFactory();

    /**
     * Builds the {@code SessionFactory} from the default Hibernate configuration file.
     *
     * @return the configured {@code SessionFactory}
     * @throws ExceptionInInitializerError if the initialization fails
     */
    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Shuts down the {@code SessionFactory}, releasing all resources.
     */
    public static void shutdown() {
        getSessionFactory().close();
    }
}
