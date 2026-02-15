package it.hackhub.model.accounts;

import it.hackhub.model.valueobjs.Email;
import it.hackhub.model.valueobjs.UserPassword;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "admins")
@Getter @Setter
@NoArgsConstructor
public class Admin extends Account {

    public Admin(Email email, UserPassword password) {
        this.email = email;
        this.password = password;
    }
}