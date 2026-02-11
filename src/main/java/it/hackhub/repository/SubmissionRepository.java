package it.hackhub.repository;

import it.hackhub.model.Submission;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class SubmissionRepository extends AbstractRepository<Submission, Long> {

    public SubmissionRepository() {
        super(Submission.class);
    }

    public Optional<Submission> getByDeliveryAndTeam(Session session, Long deliveryId, Long pTeamId) {
        String hql = "FROM Submission s WHERE s.delivery.id = :did AND s.participatingTeam.id = :ptid";
        return session.createQuery(hql, Submission.class)
                .setParameter("did", deliveryId)
                .setParameter("ptid", pTeamId)
                .uniqueResultOptional();
    }

    public List<Submission> getByHackathonIdOrderedByDelivery(Session session, Long hackathonId) {
        String hql = "FROM Submission s WHERE s.participatingTeam.hackathon.id = :hid ORDER BY s.delivery.id";
        return session.createQuery(hql, Submission.class)
                .setParameter("hid", hackathonId)
                .list();
    }
}