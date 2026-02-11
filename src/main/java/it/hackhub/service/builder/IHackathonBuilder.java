package it.hackhub.service.builder;

import it.hackhub.model.Delivery;
import it.hackhub.model.Hackathon;
import java.time.LocalDate;

public interface IHackathonBuilder {

    void reset();

    void buildGeneralInfo(String name, String type, String prize, boolean isOnline, String location);

    void buildExecutionDates(LocalDate start, LocalDate end);

    void buildSubscriptionDates(LocalDate start, LocalDate end);

    void validateDates();

    void buildRules(int maxTeamSize, String ruleDocument);

    void buildResponseType(String responseType);

    void addDelivery(Delivery delivery);

    Hackathon getResult();
}