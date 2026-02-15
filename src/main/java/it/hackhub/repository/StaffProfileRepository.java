package it.hackhub.repository;

import it.hackhub.model.accounts.StaffProfile;
import org.hibernate.Session;
import java.util.Optional;

public class StaffProfileRepository extends AbstractRepository<StaffProfile, Long> {

    public StaffProfileRepository() {
        super(StaffProfile.class);
    }

    public Optional<StaffProfile> findByEmail(Session session, String email) {
        String hql = "FROM StaffProfile s WHERE s.email.value = :email";
        return session.createQuery(hql, StaffProfile.class)
                .setParameter("email", email)
                .uniqueResultOptional();
    }
}