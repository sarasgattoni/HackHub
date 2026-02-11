package it.hackhub.service;

import it.hackhub.model.*;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SubmissionHandler {

    private final HackathonRepository hackathonRepo = new HackathonRepository();
    private final UserRepository userRepo = new UserRepository();
    private final TeamRepository teamRepo = new TeamRepository();
    private final SubmissionRepository submissionRepo = new SubmissionRepository();
    private final StaffProfileRepository staffRepo = new StaffProfileRepository();
    private final StaffAssignmentRepository staffAssignRepo = new StaffAssignmentRepository();
    private final DeliveryRepository deliveryRepo = new DeliveryRepository();
    private final ParticipatingTeamRepository partTeamRepo = new ParticipatingTeamRepository();

    public List<String> getHackathonDeliveries(Long hackathonId, Long userId) {
        return HibernateExecutor.execute(session -> {
            Hackathon hackathon = hackathonRepo.findById(session, hackathonId)
                    .orElseThrow(() -> new IllegalArgumentException("Hackathon not found"));

            if (!hackathon.isInExecution()) {
                throw new IllegalStateException("Hackathon not in execution phase");
            }

            User user = userRepo.findById(session, userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Team team = teamRepo.getTeamOfUser(session, userId).orElse(null);
            if (team == null) {
                throw new IllegalStateException("User must be in a team to consult deliveries");
            }

            partTeamRepo.findByTeamAndHackathon(session, team.getId(), hackathonId)
                    .orElseThrow(() -> new IllegalStateException("Your team is not subscribed to the hackathon"));

            List<String> texts = new ArrayList<>();
            for (Delivery d : hackathon.getDeliveries()) {
                texts.add(d.getText());
            }
            return texts;
        });
    }

    public void sendSubmission(Long hackathonId, Long deliveryId, Long userId, String content) {
        HibernateExecutor.executeVoidTransaction(session -> {
            Hackathon hackathon = hackathonRepo.findById(session, hackathonId)
                    .orElseThrow(() -> new IllegalArgumentException("Hackathon not found"));

            if (!hackathon.isInExecution()) {
                throw new IllegalStateException("Hackathon not in execution phase");
            }

            User user = userRepo.findById(session, userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Team team = teamRepo.getTeamOfUser(session, userId)
                    .orElseThrow(() -> new IllegalStateException("User without team"));

            if (!team.isLeader(user)) {
                throw new IllegalStateException("Only the team leader can submit solutions");
            }

            ParticipatingTeam pt = partTeamRepo.findByTeamAndHackathon(session, team.getId(), hackathonId)
                    .orElseThrow(() -> new IllegalStateException("Team does not participate in the hackathon"));

            Delivery delivery = deliveryRepo.findById(session, deliveryId)
                    .orElseThrow(() -> new IllegalArgumentException("Delivery not found"));

            Optional<Submission> existing = submissionRepo.getByDeliveryAndTeam(session, deliveryId, pt.getId());

            if (existing.isPresent()) {
                Submission sub = existing.get();
                sub.update(content);
                submissionRepo.save(session, sub);
            } else {
                Submission sub = new Submission(pt, delivery);
                sub.update(content);
                submissionRepo.save(session, sub);
            }
        });
    }

    public List<Submission> getHackathonSubmissions(Long hackathonId, Long staffId) {
        return HibernateExecutor.execute(session -> {
            StaffProfile staff = staffRepo.findById(session, staffId)
                    .orElseThrow(() -> new IllegalArgumentException("Staff profile not found"));

            List<StaffAssignment> assignments = staffAssignRepo.findByStaffProfile(session, staffId);
            boolean hasAccess = assignments.stream()
                    .anyMatch(a -> a.getHackathon().getId().equals(hackathonId));

            if (!hasAccess) {
                throw new IllegalAccessError("Staff member not assigned to this hackathon");
            }

            return submissionRepo.getByHackathonIdOrderedByDelivery(session, hackathonId);
        });
    }
}