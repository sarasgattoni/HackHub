package it.hackhub.model.notifications;

import it.hackhub.model.accounts.Account;

public class SystemNotificationFactory implements NotificationFactory {
    @Override
    public Notification createNotification(Account recipient, String content) {
        SystemNotification n = new SystemNotification();
        n.setRecipient(recipient);
        n.setContent(content);
        return n;
    }
}