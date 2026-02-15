package it.hackhub.model.notifications;

import it.hackhub.model.accounts.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "notification_type")
@Getter
@Setter
public abstract class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    private LocalDateTime timestamp;
    private boolean isRead;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private Account recipient;

    public Notification() {
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }
}