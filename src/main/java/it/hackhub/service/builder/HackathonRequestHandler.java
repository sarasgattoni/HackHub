package it.hackhub.service.builder;

import it.hackhub.dtos.CreateHackathonRequest;
import it.hackhub.dtos.DeliveryDTO;
import it.hackhub.model.*;
import it.hackhub.service.builder.IHackathonBuilder;
import it.hackhub.service.builder.HackathonConcreteBuilder;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.repository.HackathonRepository;
import it.hackhub.repository.HackathonRequestRepository;
import it.hackhub.repository.StaffProfileRepository;

public class HackathonRequestHandler {

    private final HackathonRepository hackathonRepo = new HackathonRepository();
    private final HackathonRequestRepository hackathonRequestRepo = new HackathonRequestRepository();
    private final StaffProfileRepository staffProfileRepo = new StaffProfileRepository();

    public void createHackathonRequest(CreateHackathonRequest infos, Long staffMemberId) {

        HibernateExecutor.executeVoidTransaction(session -> {

            if (hackathonRepo.existsByName(session, infos.getName())) {
                throw new IllegalArgumentException("Hackathon name is not unique");
            }

            IHackathonBuilder builder = new HackathonConcreteBuilder();

            builder.buildGeneralInfo(
                    infos.getName(), infos.getType(), infos.getPrize(),
                    infos.isOnline(), infos.getLocation()
            );

            builder.buildExecutionDates(infos.getExecutionStartDate(), infos.getExecutionEndDate());
            builder.buildSubscriptionDates(infos.getSubscriptionStartDate(), infos.getSubscriptionEndDate());

            builder.validateDates();

            builder.buildRules(infos.getMaxTeamSize(), infos.getRuleDocument());
            builder.buildResponseType(infos.getResponseType());

            if (infos.getDeliveries() != null) {
                for (DeliveryDTO delDto : infos.getDeliveries()) {
                    Delivery delivery;
                    if ("FLAG".equalsIgnoreCase(infos.getResponseType())) {
                        delivery = new FlagDelivery(delDto.getText(), delDto.getSolution());
                    } else {
                        delivery = new StandardDelivery(delDto.getText());
                    }
                    builder.addDelivery(delivery);
                }
            }

            Hackathon hackathon = builder.getResult();

            StaffProfile applicant = staffProfileRepo.findById(session, staffMemberId)
                    .orElseThrow(() -> new IllegalArgumentException("Staff member not found"));

            HackathonRequest request = new HackathonRequest(hackathon, applicant);

            hackathonRequestRepo.save(session, request);
        });
    }
}