package it.hackhub.model.notifications;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("INVITE")
@Getter
@Setter
public class InviteNotification extends Notification {

    private Long targetTeamId;
    private Long targetHackathonId;
    private String roleProposed;

}