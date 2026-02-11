package it.hackhub.repository;

import it.hackhub.model.Hackathon;
import org.hibernate.Session;

public class HackathonRepository extends AbstractRepository<Hackathon, Long> {

    public HackathonRepository() {
        super(Hackathon.class);
    }

    public boolean existsByName(Session session, String name) {
        return session.createQuery("SELECT count(h) FROM Hackathon h WHERE h.name = :name", Long.class)
                .setParameter("name", name)
                .uniqueResult() > 0;
    }
}