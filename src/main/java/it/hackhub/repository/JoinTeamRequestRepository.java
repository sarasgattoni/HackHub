package it.hackhub.repository;

import it.hackhub.model.JoinTeamRequest;
import org.hibernate.Session;
import java.util.Optional;

public class JoinTeamRequestRepository extends AbstractRepository<JoinTeamRequest, Long> {

    public JoinTeamRequestRepository() {
        super(JoinTeamRequest.class);
    }

    public Optional<JoinTeamRequest> findPendingRequest(Session session, Long teamId, Long userId) {
        String hql = "FROM JoinTeamRequest r WHERE r.team.id = :tid AND r.recipient.id = :uid AND r.state = 'PENDING'";
        return session.createQuery(hql, JoinTeamRequest.class)
                .setParameter("tid", teamId)
                .setParameter("uid", userId)
                .uniqueResultOptional();
    }
}