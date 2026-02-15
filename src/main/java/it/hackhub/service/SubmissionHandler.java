package it.hackhub.service;

import it.hackhub.model.*;
import it.hackhub.model.accounts.StaffProfile;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.*;
import java.util.List;

public class SubmissionHandler {

    private final HackathonRepository hackathonRepo = new HackathonRepository();
    private final StaffProfileRepository staffRepo = new StaffProfileRepository();
    private final DeliveryRepository deliveryRepo = new DeliveryRepository();
    private final ParticipatingTeamRepository partTeamRepo = new ParticipatingTeamRepository();
    private final TeamRepository teamRepo = new TeamRepository();

    public void submitSolution(Long userId, Long hackathonId, Long deliveryId, String solutionUrl) {
        HibernateExecutor.executeVoidTransaction(session -> {

            Hackathon hackathon = hackathonRepo.findById(session, hackathonId)
                    .orElseThrow(() -> new IllegalArgumentException("Hackathon not found"));

            Delivery delivery = deliveryRepo.findById(session, deliveryId)
                    .orElseThrow(() -> new IllegalArgumentException("Delivery not found"));

            Team team = teamRepo.getTeamOfUser(session, userId)
                    .orElseThrow(() -> new IllegalArgumentException("User does not belong to any team"));

            ParticipatingTeam participatingTeam = partTeamRepo.findByTeamAndHackathon(session, team.getId(), hackathonId)
                    .orElseThrow(() -> new IllegalArgumentException("Team is not participating in this hackathon"));

            StaffProfile actor = staffRepo.findById(session, userId).orElse(null);

            hackathon.submitSolution(actor, participatingTeam, delivery, solutionUrl);

            hackathonRepo.save(session, hackathon);
        });
    }

    public List<String> getHackathonDeliveries(Long hackathonId, Long userId) {
        return HibernateExecutor.execute(session -> {
            Hackathon hackathon = hackathonRepo.findById(session, hackathonId)
                    .orElseThrow(() -> new IllegalArgumentException("Hackathon not found"));

            StaffProfile actor = staffRepo.findById(session, userId).orElse(null);

            return hackathon.viewDeliveries(actor);
        });
    }
}