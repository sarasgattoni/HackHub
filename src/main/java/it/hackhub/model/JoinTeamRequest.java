package it.hackhub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "join_team_requests")
@Getter @Setter
@NoArgsConstructor
public class JoinTeamRequest extends Request {

    @ManyToOne(optional = false)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    public JoinTeamRequest(Team team, User recipient) {
        this.team = team;
        this.recipient = recipient;
    }
}