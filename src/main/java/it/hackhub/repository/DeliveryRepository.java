package it.hackhub.repository;

import it.hackhub.model.Delivery;

public class DeliveryRepository extends AbstractRepository<Delivery, Long> {

    public DeliveryRepository() {
        super(Delivery.class);
    }

}