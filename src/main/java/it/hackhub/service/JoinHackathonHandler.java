package it.hackhub.service;

import it.hackhub.model.*;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.*;

public class JoinHackathonHandler {

    private final HackathonRepository hackathonRepo = new HackathonRepository();
    private final UserRepository userRepo = new UserRepository();
    private final TeamRepository teamRepo = new TeamRepository();

    public void joinHackathon(Long userId, Long hackathonId) {
        HibernateExecutor.executeVoidTransaction(session -> {

            Hackathon hackathon = hackathonRepo.findById(session, hackathonId)
                    .orElseThrow(() -> new IllegalArgumentException("Hackathon not found"));

            User user = userRepo.findById(session, userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Team team = teamRepo.getTeamOfUser(session, userId)
                    .orElseThrow(() -> new IllegalStateException("You must be in a team to participate"));

            if (!team.isLeader(user)) {
                throw new IllegalStateException("Only the team leader can subscribe the team");
            }

            hackathon.enrollTeam(team);

            hackathonRepo.save(session, hackathon);
        });
    }
}