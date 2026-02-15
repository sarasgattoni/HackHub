package it.hackhub.repository;

import it.hackhub.model.notifications.Notification;
import org.hibernate.Session;
import java.util.List;

public class NotificationRepository extends AbstractRepository<Notification, Long> {

    public NotificationRepository() {
        super(Notification.class);
    }

    public Class<Notification> getEntityClass() {
        return Notification.class;
    }

    public List<Notification> findByRecipient(Session session, Long recipientId) {
        return session.createQuery("FROM Notification n WHERE n.recipient.id = :uid ORDER BY n.timestamp DESC", Notification.class)
                .setParameter("uid", recipientId)
                .list();
    }

    public List<Notification> findUnread(Session session, Long recipientId) {
        return session.createQuery("FROM Notification n WHERE n.recipient.id = :uid AND n.isRead = false", Notification.class)
                .setParameter("uid", recipientId)
                .list();
    }
}