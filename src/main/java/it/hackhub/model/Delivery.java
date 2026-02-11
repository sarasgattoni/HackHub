package it.hackhub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "delivery_type")
public abstract class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Getter @Setter
    protected String text;

    @ManyToOne
    @JoinColumn(name = "hackathon_id")
    @Setter @Getter
    protected Hackathon hackathon;
}