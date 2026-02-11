package it.hackhub.service;

import it.hackhub.model.*;
import it.hackhub.model.enums.RequestState;
import it.hackhub.model.enums.StaffRole;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.*;

import java.util.Optional;

public class RoleHandler {

    private final StaffProfileRepository staffRepo = new StaffProfileRepository();
    private final HackathonRepository hackathonRepo = new HackathonRepository();
    private final RoleRequestRepository roleReqRepo = new RoleRequestRepository();
    private final StaffAssignmentRepository staffAssignRepo = new StaffAssignmentRepository();

    public void sendRoleRequest(Long organizerId, String targetEmail, Long hackathonId, StaffRole roleToAssign) {
        HibernateExecutor.executeVoidTransaction(session -> {

            StaffProfile organizer = staffRepo.findById(session, organizerId)
                    .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));

            StaffProfile target = staffRepo.findByEmail(session, targetEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Nominee not found with this email"));

            Hackathon hackathon = hackathonRepo.findById(session, hackathonId)
                    .orElseThrow(() -> new IllegalArgumentException("Hackathon not found"));

            assertIsOrganizer(session, organizerId, hackathonId);

            hackathon.assertApprovedOrSubscription();

            RoleRequest request = new RoleRequest(hackathon, target, roleToAssign);

            roleReqRepo.save(session, request);
        });
    }

    public void selfAssignRole(Long organizerId, Long hackathonId, StaffRole roleToAssign) {
        HibernateExecutor.executeVoidTransaction(session -> {

            StaffProfile organizer = staffRepo.findById(session, organizerId)
                    .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));

            Hackathon hackathon = hackathonRepo.findById(session, hackathonId)
                    .orElseThrow(() -> new IllegalArgumentException("Hackathon not found"));

            assertIsOrganizer(session, organizerId, hackathonId);
            hackathon.assertApprovedOrSubscription();

            replaceAndAssignRole(session, organizer, hackathon, roleToAssign);
        });
    }

    public void evaluateRoleRequest(Long requestId, boolean isAccepted) {
        HibernateExecutor.executeVoidTransaction(session -> {

            RoleRequest request = roleReqRepo.findById(session, requestId)
                    .orElseThrow(() -> new IllegalArgumentException("Request not found"));

            if (request.getState() != RequestState.PENDING) {
                throw new IllegalStateException("The request has already been processed");
            }

            if (isAccepted) {
                replaceAndAssignRole(session, request.getCandidate(), request.getHackathon(), request.getRole());
                request.setState(RequestState.APPROVED);
            } else {
                request.setState(RequestState.DENIED);
            }

            roleReqRepo.save(session, request);
        });
    }

    private void assertIsOrganizer(org.hibernate.Session session, Long staffId, Long hackathonId) {
        Optional<StaffAssignment> orgAssignment = staffAssignRepo.findByHackathonAndRole(session, hackathonId, StaffRole.ORGANIZER);

        if (orgAssignment.isEmpty() || !orgAssignment.get().getStaffProfile().getId().equals(staffId)) {
            throw new IllegalAccessError("Only the Hackathon Organizer can nominate staff");
        }
    }

    private void replaceAndAssignRole(org.hibernate.Session session, StaffProfile newStaff, Hackathon hackathon, StaffRole role) {

        Optional<StaffAssignment> existingAssignment = staffAssignRepo.findByHackathonAndRole(session, hackathon.getId(), role);

        existingAssignment.ifPresent(assignment -> staffAssignRepo.delete(session, assignment));

        StaffAssignment newAssignment = new StaffAssignment(newStaff, hackathon, role);

        staffAssignRepo.save(session, newAssignment);
    }
}