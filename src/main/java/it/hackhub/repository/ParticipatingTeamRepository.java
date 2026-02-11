package it.hackhub.repository;

import it.hackhub.model.ParticipatingTeam;
import org.hibernate.Session;
import java.util.Optional;

public class ParticipatingTeamRepository extends AbstractRepository<ParticipatingTeam, Long> {

    public ParticipatingTeamRepository() {
        super(ParticipatingTeam.class);
    }

    public Optional<ParticipatingTeam> findByTeamAndHackathon(Session session, Long teamId, Long hackathonId) {
        String hql = "FROM ParticipatingTeam pt WHERE pt.team.id = :tid AND pt.hackathon.id = :hid";
        return session.createQuery(hql, ParticipatingTeam.class)
                .setParameter("tid", teamId)
                .setParameter("hid", hackathonId)
                .uniqueResultOptional();
    }
}