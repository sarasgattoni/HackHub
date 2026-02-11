package it.hackhub.repository;

import it.hackhub.model.SupportChat;
import org.hibernate.Session;
import java.util.Optional;

public class SupportChatRepository extends AbstractRepository<SupportChat, Long> {
    public SupportChatRepository() {
        super(SupportChat.class);
    }

    public Optional<SupportChat> findByTeamAndMentor(Session session, Long participatingTeamId, Long mentorId) {
        String hql = "FROM SupportChat sc WHERE sc.team.id = :tid AND sc.mentor.id = :mid";
        return session.createQuery(hql, SupportChat.class)
                .setParameter("tid", participatingTeamId)
                .setParameter("mid", mentorId)
                .uniqueResultOptional();
    }
}