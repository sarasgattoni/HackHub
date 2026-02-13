package it.hackhub.service.builder;

import it.hackhub.model.*;
import it.hackhub.model.enums.ResponseType;
import it.hackhub.model.validators.HackathonDomainValidator;
import it.hackhub.model.valueobjs.Info;
import it.hackhub.model.valueobjs.Period;
import it.hackhub.model.valueobjs.Rules;

import java.util.ArrayList;
import java.util.List;

public class HackathonConcreteBuilder implements IHackathonBuilder {

    private Hackathon hackathon;

    private Info info;
    private Period executionPeriod;
    private Period subscriptionPeriod;
    private Rules rules;
    private ResponseType responseType;
    private List<DeliveryTmp> deliveriesTmp;

    private final HackathonDomainValidator validator = new HackathonDomainValidator();

    public HackathonConcreteBuilder() {
        this.reset();
    }

    @Override
    public void reset() {
        this.hackathon = new Hackathon();
        this.deliveriesTmp = new ArrayList<>();
        this.info = null;
        this.executionPeriod = null;
        this.subscriptionPeriod = null;
        this.rules = null;
        this.responseType = null;
    }

    @Override
    public void buildGeneralInfo(Info info) {
        this.info = info;
    }

    @Override
    public void buildExecutionDates(Period period) {
        this.executionPeriod = period;
    }

    @Override
    public void buildSubscriptionDates(Period period) {
        this.subscriptionPeriod = period;
    }

    @Override
    public void buildRules(Rules rules) {
        this.rules = rules;
    }

    @Override
    public void buildResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    @Override
    public void addDeliveries(List<DeliveryTmp> deliveries) {
        if (deliveries != null) {
            this.deliveriesTmp = deliveries;
        }
    }

    @Override
    public Hackathon getResult() {
        validator.validateAll(info, rules, executionPeriod, subscriptionPeriod, responseType, deliveriesTmp);

        hackathon.setName(info.getName());
        hackathon.setType(info.getType());
        hackathon.setPrize(String.valueOf(info.getPrize()));
        hackathon.setOnline(info.isOnline());

        hackathon.setMaxTeamSize(rules.getMaxTeamMembers());
        hackathon.setRuleDocument(rules.getRulesText());

        hackathon.setExecutionStartDate(executionPeriod.getStartDate().toLocalDate());
        hackathon.setExecutionEndDate(executionPeriod.getEndDate().toLocalDate());
        hackathon.setSubscriptionStartDate(subscriptionPeriod.getStartDate().toLocalDate());
        hackathon.setSubscriptionEndDate(subscriptionPeriod.getEndDate().toLocalDate());

        hackathon.setResponseType(responseType.name());

        if (deliveriesTmp != null) {
            for (DeliveryTmp tmp : deliveriesTmp) {
                Delivery deliveryEntity;

                if (responseType == ResponseType.FLAG) {
                    deliveryEntity = new FlagDelivery(tmp.getText(), tmp.getSolution());
                } else {
                    deliveryEntity = new StandardDelivery(tmp.getText());
                }

                hackathon.addDelivery(deliveryEntity);
            }
        }

        Hackathon product = this.hackathon;
        this.reset();
        return product;
    }
}