package it.hackhub.service;

import it.hackhub.model.StaffProfile;
import it.hackhub.model.enums.StaffRole;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.model.valueobjs.Email;
import it.hackhub.model.valueobjs.UserPassword;
import it.hackhub.repository.StaffProfileRepository;

public class StaffProfileHandler {

    private final StaffProfileRepository staffRepo = new StaffProfileRepository();

    public void createStaffProfile(String name, String surname, String emailValue, String passwordValue, StaffRole role) {
        HibernateExecutor.executeVoidTransaction(session -> {

            if (staffRepo.findByEmail(session, emailValue).isPresent()) {
                throw new IllegalArgumentException("An account already exists with this email");
            }

            Email email = new Email(emailValue);
            UserPassword password = new UserPassword(passwordValue);

            StaffProfile newProfile = new StaffProfile(name, surname, email, password, role);

            staffRepo.save(session, newProfile);
        });
    }

}