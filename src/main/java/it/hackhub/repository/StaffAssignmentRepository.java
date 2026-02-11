package it.hackhub.repository;

import it.hackhub.model.StaffAssignment;
import it.hackhub.model.enums.StaffRole;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class StaffAssignmentRepository extends AbstractRepository<StaffAssignment, Long> {

    public StaffAssignmentRepository() {
        super(StaffAssignment.class);
    }

    public List<StaffAssignment> findByStaffProfile(Session session, Long staffProfileId) {
        String hql = "FROM StaffAssignment sa WHERE sa.staffProfile.id = :sid";
        return session.createQuery(hql, StaffAssignment.class)
                .setParameter("sid", staffProfileId)
                .list();
    }

    public Optional<StaffAssignment> findByHackathonAndRole(Session session, Long hackathonId, StaffRole role) {
        String hql = "FROM StaffAssignment sa WHERE sa.hackathon.id = :hid AND sa.role = :role";
        return session.createQuery(hql, StaffAssignment.class)
                .setParameter("hid", hackathonId)
                .setParameter("role", role)
                .uniqueResultOptional();
    }
}