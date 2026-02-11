package it.hackhub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "hackathon_requests")
@Getter @Setter
@NoArgsConstructor
public class HackathonRequest extends Request {

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "hackathon_id", referencedColumnName = "id")
    private Hackathon hackathon;

    @ManyToOne
    @JoinColumn(name = "applicant_id")
    private StaffProfile applicant;

    public HackathonRequest(Hackathon hackathon, StaffProfile applicant) {
        this.hackathon = hackathon;
        this.applicant = applicant;
    }
}