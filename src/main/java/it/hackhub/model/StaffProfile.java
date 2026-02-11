package it.hackhub.model;

import it.hackhub.model.enums.StaffRole;
import it.hackhub.model.valueobjs.Email;
import it.hackhub.model.valueobjs.UserPassword;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "staff_profiles")
@Getter @Setter
@NoArgsConstructor
public class StaffProfile extends Account {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String surname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StaffRole role;

    public StaffProfile(String name, String surname, Email email, UserPassword password, StaffRole role) {
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = password;
        this.role = role;
    }
}