package it.hackhub.service;

import it.hackhub.model.*;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.*;

public class TeamHandler {

    private final UserRepository userRepo = new UserRepository();
    private final TeamRepository teamRepo = new TeamRepository();
    private final JoinTeamRequestRepository joinReqRepo = new JoinTeamRequestRepository();

    public void createTeam(Long userId, String teamName) {
        HibernateExecutor.executeVoidTransaction(session -> {

            User user = userRepo.findById(session, userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            if (teamRepo.getTeamOfUser(session, userId).isPresent()) {
                throw new IllegalStateException("User already in another team");
            }

            if (teamRepo.existsByName(session, teamName)) {
                throw new IllegalArgumentException("Team name is already used");
            }

            Team team = new Team(teamName, user);

            teamRepo.save(session, team);
        });
    }

    public void inviteUserToTeam(Long senderId, String recipientUsername) {
        HibernateExecutor.executeVoidTransaction(session -> {

            User recipient = userRepo.findByUsername(session, recipientUsername)
                    .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));

            User sender = userRepo.findById(session, senderId)
                    .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

            Team team = teamRepo.getTeamOfUser(session, senderId)
                    .orElseThrow(() -> new IllegalStateException("Sender is not in a team"));

            if (!team.isLeader(sender)) {
                throw new IllegalStateException("Only team leader can send invites");
            }

            JoinTeamRequest request = new JoinTeamRequest(team, recipient);

            joinReqRepo.save(session, request);
        });
    }
}