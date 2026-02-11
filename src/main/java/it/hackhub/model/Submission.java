package it.hackhub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "submissions")
@Getter @Setter
@NoArgsConstructor
public class Submission {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ParticipatingTeam participatingTeam;

    @ManyToOne(optional = false)
    private Delivery delivery;

    @Lob
    private String content;

    private Integer score;
    private String writtenEvaluation;

    @ManyToOne
    private StaffProfile judge;

    public Submission(ParticipatingTeam pt, Delivery d) {
        this.participatingTeam = pt;
        this.delivery = d;
    }

    public void update(String content) {
        this.content = content;
    }

    public void evaluate(Integer score, String writtenEvaluation, StaffProfile judge) {
        if (score < 0 || score > 100) throw new IllegalArgumentException("Score invalido"); // Invariant
        this.score = score;
        this.writtenEvaluation = writtenEvaluation;
        this.judge = judge;
    }
}