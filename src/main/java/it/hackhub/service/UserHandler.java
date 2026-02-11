package it.hackhub.service;

import it.hackhub.model.*;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.*;

public class UserHandler {

    private final UserRepository userRepo = new UserRepository();
    private final TeamRepository teamRepo = new TeamRepository();
    private final JoinTeamRequestRepository joinRequestRepo = new JoinTeamRequestRepository();

    public void inviteUserToTeam(Long recipientId, Long senderId) {
        HibernateExecutor.executeVoidTransaction(session -> {
            User recipient = userRepo.findById(session, recipientId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            User sender = userRepo.findById(session, senderId)
                    .orElseThrow(() -> new IllegalArgumentException("Sender not found"));

            Team team = teamRepo.getTeamOfUser(session, senderId)
                    .orElseThrow(() -> new IllegalStateException("Sender is not in a team"));

            if (!team.isLeader(sender)) {
                throw new IllegalStateException("Only the leader can invite members");
            }

            JoinTeamRequest request = new JoinTeamRequest(team, recipient);

            joinRequestRepo.save(session, request);
        });
    }
}