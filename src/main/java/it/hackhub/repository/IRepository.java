package it.hackhub.repository;

import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

    public interface IRepository<T, ID> {
        Optional<T> findById(Session session, ID id);
        List<T> findAll(Session session);
        void save(Session session, T entity);
        void delete(Session session, T entity);
    }
