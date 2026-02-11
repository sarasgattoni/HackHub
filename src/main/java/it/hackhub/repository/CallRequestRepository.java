package it.hackhub.repository;

import it.hackhub.model.CallRequest;

public class CallRequestRepository extends AbstractRepository<CallRequest, Long> {
    public CallRequestRepository() {
        super(CallRequest.class);
    }
}