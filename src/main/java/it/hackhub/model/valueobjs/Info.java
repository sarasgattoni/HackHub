package it.hackhub.model.valueobjs;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@EqualsAndHashCode
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Info {

    @Column(nullable = false)
    private String name;

    private String type;

    private String subtype;

    @Column(nullable = false)
    private double prize;

    @Column(nullable = false)
    private boolean isOnline;

    public Info(String name, String type, String subtype, double prize, boolean isOnline) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("The name of the it.hackhub.model.Hackathon is obligatory");
        }

        if (prize < 0) {
            throw new IllegalArgumentException("The prize of the it.hackhub.model.Hackathon can't be negative");
        }

        this.name = name;
        this.type = type;
        this.subtype = (subtype != null) ? subtype : "";
        this.prize = prize;
        this.isOnline = isOnline;
    }
}
