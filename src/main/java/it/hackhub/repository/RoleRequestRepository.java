package it.hackhub.repository;

import it.hackhub.model.RoleRequest;

public class RoleRequestRepository extends AbstractRepository<RoleRequest, Long> {

    public RoleRequestRepository() {
        super(RoleRequest.class);
    }

}