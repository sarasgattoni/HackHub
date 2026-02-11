package it.hackhub.service;

import it.hackhub.model.Admin;
import it.hackhub.model.Hackathon;
import it.hackhub.model.HackathonRequest;
import it.hackhub.model.StaffProfile;
import it.hackhub.model.enums.RequestState;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.AdminRepository;
import it.hackhub.repository.HackathonRepository;
import it.hackhub.repository.HackathonRequestRepository;
import it.hackhub.repository.StaffProfileRepository;

public class HackathonRequestHandler {

    private final HackathonRequestRepository requestRepo = new HackathonRequestRepository();
    private final AdminRepository adminRepo = new AdminRepository();
    private final StaffProfileRepository staffRepo = new StaffProfileRepository();
    private final HackathonRepository hackathonRepo = new HackathonRepository();

    public void submitHackathonRequest(Long organizerId, Hackathon newHackathon) {
        HibernateExecutor.executeVoidTransaction(session -> {

            StaffProfile organizer = staffRepo.findById(session, organizerId)
                    .orElseThrow(() -> new IllegalArgumentException("StaffProfile not found"));

            hackathonRepo.save(session, newHackathon);

            HackathonRequest request = new HackathonRequest(newHackathon, organizer);

            requestRepo.save(session, request);
        });
    }

    public void evaluateHackathonRequest(Long adminId, Long requestId, boolean isAccepted) {
        HibernateExecutor.executeVoidTransaction(session -> {

            Admin admin = adminRepo.findById(session, adminId)
                    .orElseThrow(() -> new IllegalAccessError("Only admins can perform this action"));

            HackathonRequest request = requestRepo.findById(session, requestId)
                    .orElseThrow(() -> new IllegalArgumentException("Request not found"));

            if (request.getState() != RequestState.PENDING) {
                throw new IllegalStateException("This request has already been considered");
            }

            if (isAccepted) {
                request.setState(RequestState.APPROVED);
            } else {
                request.setState(RequestState.DENIED);
            }

            requestRepo.save(session, request);
        });
    }
}