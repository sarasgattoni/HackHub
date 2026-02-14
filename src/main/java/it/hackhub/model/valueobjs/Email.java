package it.hackhub.model.valueobjs;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.util.regex.Pattern;

@Embeddable
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public final class Email {

    private String value;

    private static final Pattern EmailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email value cannot be null or empty");
        }
        if (!EmailPattern.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email address: " +  value);
        }
        this.value = value;
    }

    public String getAddress() {
        return this.value;
    }
}