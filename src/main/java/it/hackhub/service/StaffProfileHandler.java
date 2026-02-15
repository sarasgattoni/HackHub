package it.hackhub.service;

import it.hackhub.model.accounts.AccountFactory;
import it.hackhub.model.accounts.StaffFactory;
import it.hackhub.model.accounts.Account;
import it.hackhub.model.accounts.StaffProfile;
import it.hackhub.model.valueobjs.Email;
import it.hackhub.model.valueobjs.UserPassword;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.StaffProfileRepository;

public class StaffProfileHandler {

    private final StaffProfileRepository staffRepo = new StaffProfileRepository();

    public void registerStaffProfile(String name, String surname, String emailStr, String pwdStr) {
        HibernateExecutor.executeVoidTransaction(session -> {
            AccountFactory factory = new StaffFactory();

            Account newStaff = factory.createAccount(
                    name,
                    surname,
                    new Email(emailStr),
                    new UserPassword(pwdStr)
            );

            staffRepo.save(session, (StaffProfile) newStaff);
        });
    }
}