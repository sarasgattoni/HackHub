package it.hackhub.repository;

import it.hackhub.model.accounts.Admin;

public class AdminRepository extends AbstractRepository<Admin, Long> {

    public AdminRepository() {
        super(Admin.class);
    }
}