package it.hackhub.model.notifications;

import it.hackhub.model.accounts.Account;

public class InviteNotificationFactory implements NotificationFactory {

    private Long teamId;
    private Long hackathonId;
    private String role;
    private final boolean isTeamInvite;

    public InviteNotificationFactory(Long teamId) {
        this.teamId = teamId;
        this.isTeamInvite = true;
    }

    public InviteNotificationFactory(Long hackathonId, String role) {
        this.hackathonId = hackathonId;
        this.role = role;
        this.isTeamInvite = false;
    }

    @Override
    public Notification createNotification(Account recipient, String content) {
        InviteNotification n = new InviteNotification();
        n.setRecipient(recipient);
        n.setContent(content);

        if (isTeamInvite) {
            n.setTargetTeamId(this.teamId);
        } else {
            n.setTargetHackathonId(this.hackathonId);
            n.setRoleProposed(this.role);
        }

        return n;
    }
}