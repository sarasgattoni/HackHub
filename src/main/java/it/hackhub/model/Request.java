package it.hackhub.model;

import it.hackhub.model.enums.RequestState;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter @Setter
public abstract class Request {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    protected RequestState state = RequestState.PENDING;
}