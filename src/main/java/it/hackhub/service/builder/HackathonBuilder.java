package it.hackhub.service.builder;

import it.hackhub.model.Hackathon;
import it.hackhub.model.Delivery;

import java.time.LocalDate;

public class HackathonBuilder {

    private Hackathon hackathon;

    public HackathonBuilder() {
        this.hackathon = new Hackathon();
    }

    public void buildGeneralInfo(String name, String type, String prize, boolean isOnline, String location) {
        hackathon.setName(name);
        hackathon.setType(type);
        hackathon.setPrize(prize);
        hackathon.setOnline(isOnline);
        hackathon.setLocation(location);
    }

    public void buildExecutionDates(LocalDate start, LocalDate end) {
        hackathon.setExecutionStartDate(start);
        hackathon.setExecutionEndDate(end);
    }

    public void buildSubscriptionDates(LocalDate start, LocalDate end) {
        hackathon.setSubscriptionStartDate(start);
        hackathon.setSubscriptionEndDate(end);
    }

    public void validateDates() {
        LocalDate subStart = hackathon.getSubscriptionStartDate();
        LocalDate subEnd = hackathon.getSubscriptionEndDate();
        LocalDate execStart = hackathon.getExecutionStartDate();
        LocalDate execEnd = hackathon.getExecutionEndDate();

        if (subStart == null || subEnd == null || execStart == null || execEnd == null) {
            throw new IllegalArgumentException("Tutte le date sono obbligatorie.");
        }

        if (subEnd.isAfter(execStart)) {
            throw new IllegalStateException("La data di fine iscrizione deve precedere l'inizio dell'Hackathon.");
        }

        if (execStart.isAfter(execEnd)) {
            throw new IllegalStateException("La data di fine esecuzione non può precedere la data di inizio.");
        }

        if (subStart.isAfter(subEnd)) {
            throw new IllegalStateException("La data di inizio iscrizione non può essere successiva alla data di fine.");
        }
    }

    public void buildRules(int maxTeamSize, String ruleDocument) {
        hackathon.setMaxTeamSize(maxTeamSize);
        hackathon.setRuleDocument(ruleDocument);
    }

    public void buildResponseType(String responseType) {
        hackathon.setResponseType(responseType);
    }

    public void addDelivery(Delivery delivery) {
        hackathon.addDelivery(delivery);
    }

    public Hackathon getResult() {
        return this.hackathon;
    }
}