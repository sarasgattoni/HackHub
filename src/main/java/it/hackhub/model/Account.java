package it.hackhub.model;

import it.hackhub.model.valueobjs.Email;
import it.hackhub.model.valueobjs.UserPassword;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter @Setter
public abstract class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "email", unique = true, nullable = false))
    })
    protected Email email;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "password_hash", nullable = false))
    })
    protected UserPassword password;

    public boolean checkPassword(String plainPassword) {
        return this.password.match(plainPassword);
    }
}