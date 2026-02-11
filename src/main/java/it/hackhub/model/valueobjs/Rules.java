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

    @Column(name = "rulesDocumentPath", nullable = false)
    private String rulesDocument;

    public Rules(int maxTeamMembers, String rulesDocument) {

        if (maxTeamMembers <= 0) {
            throw new IllegalArgumentException("Max members must be positive");
        }

        if (rulesDocument == null || rulesDocument.trim().isEmpty()) {
            throw new IllegalArgumentException("Rules document path cannot be null or empty");
        }

        this.maxTeamMembers = maxTeamMembers;
        this.rulesDocument = rulesDocument;
    }
}