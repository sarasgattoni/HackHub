package it.hackhub.model;

import it.hackhub.model.accounts.StaffProfile;
import it.hackhub.model.enums.StaffRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "role_requests")
@Getter @Setter
@NoArgsConstructor
public class RoleRequest extends Request {

    @ManyToOne private Hackathon hackathon;
    @ManyToOne private StaffProfile candidate;
    @Enumerated(EnumType.STRING) private StaffRole role;

    public RoleRequest(Hackathon h, StaffProfile p, StaffRole r) {
        this.hackathon = h;
        this.candidate = p;
        this.role = r;
    }
}