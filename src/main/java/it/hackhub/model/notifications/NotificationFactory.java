package it.hackhub.model.notifications;

import it.hackhub.model.accounts.Account;

public interface NotificationFactory {

    Notification createNotification(Account recipient, String content);

}
