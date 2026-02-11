package it.hackhub.model.valueobjs;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Rules {

    @Column(nullable = false)
    private int maxTeamMembers;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String rulesText;

    public Rules(int maxTeamMembers, String rulesText) {

        if (maxTeamMembers <= 0) {
            throw new IllegalArgumentException("Maximum team members number must be positive");
        }

        if (rulesText == null || rulesText.isBlank()) {
            throw new IllegalArgumentException("Rules can't be blank");
        }

        this.maxTeamMembers = maxTeamMembers;
        this.rulesText = rulesText;
    }
}