package it.hackhub.service;

import it.hackhub.model.*;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.*;

public class JoinHackathonHandler {

    private final HackathonRepository hackathonRepo = new HackathonRepository();
    private final UserRepository userRepo = new UserRepository();
    private final TeamRepository teamRepo = new TeamRepository();
    private final ParticipatingTeamRepository partTeamRepo = new ParticipatingTeamRepository();

    public void joinHackathon(Long userId, Long hackathonId) {
        HibernateExecutor.executeVoidTransaction(session -> {

            Hackathon hackathon = hackathonRepo.findById(session, hackathonId)
                    .orElseThrow(() -> new IllegalArgumentException("Hackathon not found"));

            hackathon.assertIsOpenForSubscription();

            User user = userRepo.findById(session, userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Team team = teamRepo.getTeamOfUser(session, userId)
                    .orElseThrow(() -> new IllegalStateException("You must be in a team to participate in a hackathon"));

            if (!team.isLeader(user)) {
                throw new IllegalStateException("Only the team leader can subscribe the team");
            }

            if (team.getSize() > hackathon.getMaxTeamSize()) {
                throw new IllegalStateException(
                        "Team (" + team.getSize() + " members) are over the maximum members number (" + hackathon.getMaxTeamSize() + ")"
                );
            }

            if (partTeamRepo.findByTeamAndHackathon(session, team.getId(), hackathonId).isPresent()) {
                throw new IllegalStateException("Team is already subscribed to this hackathon");
            }

            ParticipatingTeam pt = new ParticipatingTeam(team, hackathon);

            partTeamRepo.save(session, pt);
        });
    }
}