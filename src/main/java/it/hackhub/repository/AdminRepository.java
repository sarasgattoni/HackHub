package it.hackhub.repository;

import it.hackhub.model.Admin;

public class AdminRepository extends AbstractRepository<Admin, Long> {

    public AdminRepository() {
        super(Admin.class);
    }
}