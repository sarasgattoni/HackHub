package it.hackhub.model.valueobjs;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GitHubUrl {

    @Column(name = "github_url", nullable = false)
    private String value;

    public GitHubUrl(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Github URL can't be blank");
        }
        if (!value.matches("^(https?://)?(www\\.)?github\\.com/[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+/?.*$")) {
            throw new IllegalArgumentException("Must be a valid GitHub URL");
        }
        this.value = value;
    }
}