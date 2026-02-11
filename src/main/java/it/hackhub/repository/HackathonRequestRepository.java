package it.hackhub.repository;

import it.hackhub.model.HackathonRequest;

public class HackathonRequestRepository extends AbstractRepository<HackathonRequest, Long> {

    public HackathonRequestRepository() {
        super(HackathonRequest.class);
    }
}