package it.hackhub.model.valueobjs;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class UserPassword {

    @Column(name = "password", nullable = false)
    private String value;

    public UserPassword(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (value.length() < 8) {
            throw new IllegalArgumentException("Password cannot be less than 8 characters");
        }
        this.value = value;
    }

    public boolean match(String passwordAttempt) {
        return this.value.equals(passwordAttempt);
    }

    @Override
    public String toString() {
        return "********";
    }
}
