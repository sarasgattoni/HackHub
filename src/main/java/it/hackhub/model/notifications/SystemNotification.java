package it.hackhub.model.notifications;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("SYSTEM")
public class SystemNotification extends Notification {
    // Da aggiungere notifiche di sistema
}