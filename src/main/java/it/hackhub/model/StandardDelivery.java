package it.hackhub.model;

import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;

@Entity
@DiscriminatorValue("STANDARD")
public class StandardDelivery extends Delivery {

    public StandardDelivery(String text) {
        this.text = text;
    }

    public StandardDelivery() {}
}