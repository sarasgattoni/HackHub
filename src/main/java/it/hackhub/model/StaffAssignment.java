package it.hackhub.model;

import it.hackhub.model.accounts.StaffProfile;
import it.hackhub.model.enums.StaffRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor
public class StaffAssignment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne private StaffProfile staffProfile;
    @ManyToOne private Hackathon hackathon;
    @Enumerated(EnumType.STRING) private StaffRole role;

    public StaffAssignment(StaffProfile sp, Hackathon h, StaffRole r) {
        this.staffProfile = sp;
        this.hackathon = h;
        this.role = r;
    }
}