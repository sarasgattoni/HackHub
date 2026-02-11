package it.hackhub.repository;

import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public abstract class AbstractRepository<T, ID> implements IRepository<T, ID> {

    private final Class<T> entityClass;

    protected AbstractRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public Optional<T> findById(Session session, ID id) {
        return Optional.ofNullable(session.get(entityClass, id));
    }

    @Override
    public List<T> findAll(Session session) {
        return session.createQuery("from " + entityClass.getName(), entityClass).list();
    }

    @Override
    public void save(Session session, T entity) {
        session.persist(entity);
    }

    @Override
    public void delete(Session session, T entity) {
        session.remove(entity);
    }
}