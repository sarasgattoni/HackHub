package it.hackhub.model.accounts;

import it.hackhub.model.notifications.NotificationFactory;
import it.hackhub.model.notifications.Notification;
import it.hackhub.model.valueobjs.Email;
import it.hackhub.model.valueobjs.UserPassword;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
public abstract class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", unique = true, nullable = false))
    protected Email email;

    @Embedded
    @AttributeOverride(name = "password", column = @Column(name = "password", nullable = false))
    protected UserPassword password;

    @OneToMany(mappedBy = "recipient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications = new ArrayList<>();

    public Notification notify(NotificationFactory factory, String content) {
        Notification n = factory.createNotification(this, content);
        this.notifications.add(n);
        return n;
    }
}