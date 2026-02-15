package it.hackhub.service;

import it.hackhub.model.accounts.StaffProfile;
import it.hackhub.model.notifications.InviteNotificationFactory;
import it.hackhub.model.notifications.NotificationFactory;
import it.hackhub.model.*;
import it.hackhub.model.enums.StaffRole;
import it.hackhub.model.notifications.Notification;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.*;

public class RoleHandler {

    private final HackathonRepository hackathonRepo = new HackathonRepository();
    private final StaffProfileRepository staffRepo = new StaffProfileRepository();
    private final NotificationRepository notifRepo = new NotificationRepository();

    public void inviteStaffMember(Long organizerId, Long hackathonId, String targetEmail, StaffRole role) {
        HibernateExecutor.executeVoidTransaction(session -> {
            Hackathon hackathon = hackathonRepo.findById(session, hackathonId)
                    .orElseThrow(() -> new IllegalArgumentException("Hackathon not found"));

            StaffProfile organizer = staffRepo.findById(session, organizerId)
                    .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));

            if (!hackathon.isOrganizer(organizer)) {
                throw new SecurityException("Solo l'organizzatore può invitare staff.");
            }

            StaffProfile target = staffRepo.findByEmail(session, targetEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Nessun profilo staff trovato con questa email."));

            NotificationFactory notifFactory = new InviteNotificationFactory(hackathonId, role.name());
            Notification notification = notifFactory.createNotification(
                    target,
                    "Sei stato invitato all'hackathon " + hackathon.getName()
            );

        });
    }

    public void finalizeStaffing(Long hackathonId, Long organizerId) {
        HibernateExecutor.executeVoidTransaction(session -> {
            Hackathon hackathon = hackathonRepo.findById(session, hackathonId)
                    .orElseThrow(() -> new IllegalArgumentException("Hackathon not found"));

            StaffProfile organizer = staffRepo.findById(session, organizerId)
                    .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));

            if (!hackathon.isOrganizer(organizer)) {
                throw new SecurityException("Solo l'organizzatore può finalizzare lo staff.");
            }

            if (!hasAssignedRole(hackathon, StaffRole.JUDGE)) {
                hackathon.recruitStaff(organizer, organizer, StaffRole.JUDGE);
            }

            if (!hasAssignedRole(hackathon, StaffRole.MENTOR)) {
                hackathon.recruitStaff(organizer, organizer, StaffRole.MENTOR);
            }

            hackathonRepo.save(session, hackathon);
        });
    }

    private boolean hasAssignedRole(Hackathon h, StaffRole role) {
        return h.getStaffAssignments().stream()
                .anyMatch(sa -> sa.getRole() == role);
    }
}