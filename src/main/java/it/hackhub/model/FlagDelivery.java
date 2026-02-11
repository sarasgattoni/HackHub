package it.hackhub.model;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import lombok.Getter;
import lombok.Setter;

@Entity
@DiscriminatorValue("FLAG")
public class FlagDelivery extends Delivery {
    @Getter @Setter
    private String solution;

    public FlagDelivery(String text, String solution) {
        this.text = text;
        this.solution = solution;
    }

    public FlagDelivery() {}
}