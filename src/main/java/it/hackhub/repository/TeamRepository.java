package it.hackhub.repository;

import it.hackhub.model.Team;
import org.hibernate.Session;
import java.util.Optional;

public class TeamRepository extends AbstractRepository<Team, Long> {

    public TeamRepository() {
        super(Team.class);
    }

    public boolean existsByName(Session session, String name) {
        return session.createQuery("SELECT count(t) FROM Team t WHERE t.name = :name", Long.class)
                .setParameter("name", name)
                .uniqueResult() > 0;
    }

    public Optional<Team> getTeamOfUser(Session session, Long userId) {
        String hql = "SELECT t FROM Team t JOIN t.members m WHERE m.id = :uid";
        return session.createQuery(hql, Team.class)
                .setParameter("uid", userId)
                .uniqueResultOptional();
    }
}