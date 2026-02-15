package it.hackhub.service;

import it.hackhub.model.notifications.InviteNotificationFactory;
import it.hackhub.model.notifications.NotificationFactory;
import it.hackhub.model.notifications.SystemNotificationFactory;
import it.hackhub.model.Team;
import it.hackhub.model.accounts.User;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.TeamRepository;
import it.hackhub.repository.UserRepository;

public class TeamHandler {

    private final TeamRepository teamRepo = new TeamRepository();
    private final UserRepository userRepo = new UserRepository();

    public void createTeam(Long creatorId, String teamName) {
        HibernateExecutor.executeVoidTransaction(session -> {
            User creator = userRepo.findById(session, creatorId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            Team team = new Team(teamName, creator);

            NotificationFactory sysFactory = new SystemNotificationFactory();
            creator.notify(sysFactory, "Hai creato con successo il team: " + teamName);

            teamRepo.save(session, team);
        });
    }

    public void inviteMemberToTeam(Long leaderId, Long teamId, Long targetUserId) {
        HibernateExecutor.executeVoidTransaction(session -> {
            Team team = teamRepo.findById(session, teamId)
                    .orElseThrow(() -> new IllegalArgumentException("Team not found"));

            User leader = userRepo.findById(session, leaderId)
                    .orElseThrow(() -> new IllegalArgumentException("Leader not found"));

            if (!team.isLeader(leader)) {
                throw new IllegalStateException("Only the team leader can invite people");
            }

            User target = userRepo.findById(session, targetUserId)
                    .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

            NotificationFactory inviteFactory = new InviteNotificationFactory(team.getId());

            target.notify(inviteFactory, "You have been invited to join " + team.getName());

            userRepo.save(session, target);
        });
    }
}