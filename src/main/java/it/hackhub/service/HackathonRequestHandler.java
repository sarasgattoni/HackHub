package it.hackhub.service;

import it.hackhub.model.*;
import it.hackhub.model.enums.RequestState;
import it.hackhub.model.enums.ResponseType;
import it.hackhub.model.enums.StaffRole;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.model.valueobjs.Info;
import it.hackhub.model.valueobjs.Period;
import it.hackhub.model.valueobjs.Rules;
import it.hackhub.repository.HackathonRepository;
import it.hackhub.repository.HackathonRequestRepository;
import it.hackhub.repository.StaffProfileRepository;
import it.hackhub.service.builder.HackathonConcreteBuilder;
import it.hackhub.service.builder.IHackathonBuilder;

import java.util.List;

public class HackathonRequestHandler {

    private final HackathonRepository hackathonRepo = new HackathonRepository();
    private final HackathonRequestRepository hackathonRequestRepo = new HackathonRequestRepository();
    private final StaffProfileRepository staffRepo = new StaffProfileRepository();

    public List<HackathonRequest> getPendingRequests() {
        return HibernateExecutor.execute(session ->
                session.createQuery("FROM HackathonRequest r WHERE r.state = :state", HackathonRequest.class)
                        .setParameter("state", RequestState.PENDING)
                        .list()
        );
    }

    public HackathonRequest getRequestDetail(Long requestId) {
        return HibernateExecutor.execute(session ->
                hackathonRequestRepo.findById(session, requestId)
                        .orElseThrow(() -> new IllegalArgumentException("Request not found with ID: " + requestId))
        );
    }

    public void declineRequest(Long requestId, String reason) {
        HibernateExecutor.executeVoidTransaction(session -> {
            HackathonRequest request = hackathonRequestRepo.findById(session, requestId)
                    .orElseThrow(() -> new IllegalArgumentException("Request not found."));

            if (request.getState() != RequestState.PENDING) {
                throw new IllegalStateException("Cannot decline: request is not in PENDING state.");
            }

            request.setState(RequestState.DECLINED);

            Hackathon hackathonToDelete = request.getHackathon();

            if (hackathonToDelete != null) {
                request.setHackathon(null);
                hackathonRequestRepo.save(session, request);
                hackathonRepo.delete(session, hackathonToDelete);
            } else {
                hackathonRequestRepo.save(session, request);
            }
        });
    }

    public void approveRequest(Long requestId) {
        HibernateExecutor.executeVoidTransaction(session -> {
            HackathonRequest request = hackathonRequestRepo.findById(session, requestId)
                    .orElseThrow(() -> new IllegalArgumentException("Request not found."));

            if (request.getState() != RequestState.PENDING) {
                throw new IllegalStateException("Cannot approve: request is not in PENDING state.");
            }

            request.setState(RequestState.APPROVED);

            Hackathon hackathon = request.getHackathon();
            if (hackathon != null) {
                hackathon.nextState();
                hackathonRepo.save(session, hackathon);
            }

            hackathonRequestRepo.save(session, request);
        });
    }

    public void submitHackathonRequest(Long staffProfileId,
                                       Info info,
                                       Period hackathonPeriod,
                                       Period subscriptionPeriod,
                                       Rules rules,
                                       ResponseType responseType,
                                       List<DeliveryTmp> deliveriesTmp) {

        HibernateExecutor.executeVoidTransaction(session -> {
            StaffProfile organizer = staffRepo.findById(session, staffProfileId)
                    .orElseThrow(() -> new IllegalArgumentException("Staff profile not found."));

            IHackathonBuilder builder = new HackathonConcreteBuilder();

            builder.buildGeneralInfo(info);
            builder.buildExecutionDates(hackathonPeriod);
            builder.buildSubscriptionDates(subscriptionPeriod);
            builder.buildRules(rules);
            builder.buildResponseType(responseType);
            builder.addDeliveries(deliveriesTmp);

            Hackathon newHackathon = builder.getResult();

            StaffAssignment organizerAssignment = new StaffAssignment(organizer, newHackathon, StaffRole.ORGANIZER);
            newHackathon.addStaffAssignment(organizerAssignment);

            hackathonRepo.save(session, newHackathon);

            HackathonRequest request = new HackathonRequest(newHackathon, organizer);
            hackathonRequestRepo.save(session, request);
        });
    }
}