package it.hackhub.model;

import it.hackhub.model.valueobjs.Email;
import it.hackhub.model.valueobjs.UserPassword;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
public class User extends Account {

    @Column(unique = true, nullable = false)
    private String username;

    @OneToOne(mappedBy = "leader")
    private Team ledTeam;

    public User(String username, Email email, UserPassword password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}