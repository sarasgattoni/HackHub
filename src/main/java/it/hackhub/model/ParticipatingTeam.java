package it.hackhub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "participating_teams")
@Getter @Setter
@NoArgsConstructor
public class ParticipatingTeam {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Team team;

    @ManyToOne(optional = false)
    private Hackathon hackathon;

    public ParticipatingTeam(Team team, Hackathon hackathon) {
        this.team = team;
        this.hackathon = hackathon;
    }
}