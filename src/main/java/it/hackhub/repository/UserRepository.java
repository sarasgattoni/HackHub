package it.hackhub.repository;

import it.hackhub.model.User;
import org.hibernate.Session;
import java.util.Optional;

public class UserRepository extends AbstractRepository<User, Long> {

    public UserRepository() {
        super(User.class);
    }

    public Optional<User> findByUsername(Session session, String username) {
        String hql = "FROM User u WHERE u.username = :username";
        return session.createQuery(hql, User.class)
                .setParameter("username", username)
                .uniqueResultOptional();
    }
}