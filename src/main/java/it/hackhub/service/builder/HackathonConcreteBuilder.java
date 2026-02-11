package it.hackhub.service.builder;

import it.hackhub.model.Hackathon;
import it.hackhub.model.Delivery;
import java.time.LocalDate;

public class HackathonConcreteBuilder implements IHackathonBuilder {

    private Hackathon hackathon;

    public HackathonConcreteBuilder() {
        this.reset();
    }

    @Override
    public void reset() {
        this.hackathon = new Hackathon();
    }

    @Override
    public void buildGeneralInfo(String name, String type, String prize, boolean isOnline, String location) {
        hackathon.setName(name);
        hackathon.setType(type);
        hackathon.setPrize(prize);
        hackathon.setOnline(isOnline);
        hackathon.setLocation(location);
    }

    @Override
    public void buildExecutionDates(LocalDate start, LocalDate end) {
        hackathon.setExecutionStartDate(start);
        hackathon.setExecutionEndDate(end);
    }

    @Override
    public void buildSubscriptionDates(LocalDate start, LocalDate end) {
        hackathon.setSubscriptionStartDate(start);
        hackathon.setSubscriptionEndDate(end);
    }

    @Override
    public void validateDates() {
        if (hackathon.getSubscriptionStartDate() == null || hackathon.getExecutionStartDate() == null) {
            throw new IllegalArgumentException("Le date non possono essere nulle.");
        }

        if (hackathon.getSubscriptionEndDate() != null &&
                hackathon.getSubscriptionEndDate().isAfter(hackathon.getExecutionStartDate())) {
            throw new IllegalStateException("Le iscrizioni devono terminare prima dell'inizio dell'esecuzione.");
        }

        if (hackathon.getExecutionEndDate() != null &&
                hackathon.getExecutionStartDate().isAfter(hackathon.getExecutionEndDate())) {
            throw new IllegalStateException("La data di fine non può essere precedente a quella di inizio.");
        }
    }

    @Override
    public void buildRules(int maxTeamSize, String ruleDocument) {
        hackathon.setMaxTeamSize(maxTeamSize);
        hackathon.setRuleDocument(ruleDocument);
    }

    @Override
    public void buildResponseType(String responseType) {
        hackathon.setResponseType(responseType);
    }

    @Override
    public void addDelivery(Delivery delivery) {
        hackathon.addDelivery(delivery);
    }

    @Override
    public Hackathon getResult() {
        Hackathon product = this.hackathon;
        this.reset();
        return product;
    }
}