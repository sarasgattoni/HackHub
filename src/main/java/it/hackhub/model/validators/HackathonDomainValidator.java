package it.hackhub.model.validators;

import it.hackhub.model.DeliveryTmp;
import it.hackhub.model.enums.ResponseType;
import it.hackhub.model.valueobjs.Info;
import it.hackhub.model.valueobjs.Period;
import it.hackhub.model.valueobjs.Rules;

import java.util.List;

public class HackathonDomainValidator {

    public void validateAll(Info info,
                            Rules rules,
                            Period hackathonPeriod,
                            Period subscriptionPeriod,
                            ResponseType responseType,
                            List<DeliveryTmp> deliveriesTmp) {

        if (info == null) throw new IllegalArgumentException("Info are obligatory");
        if (rules == null) throw new IllegalArgumentException("Rules are obligatory");
        if (hackathonPeriod == null) throw new IllegalArgumentException("Hackathon period is obligatory");
        if (subscriptionPeriod == null) throw new IllegalArgumentException("Subscription period is obligatory");
        if (responseType == null) throw new IllegalArgumentException("Response type is obligatory");

        if (subscriptionPeriod.getEndDate().isAfter(hackathonPeriod.getStartDate())) {
            throw new IllegalStateException("The registration deadline must be before the start of the Hackathon");
        }

        if (deliveriesTmp != null && !deliveriesTmp.isEmpty()) {
            for (DeliveryTmp del : deliveriesTmp) {
                validateDeliveryConsistency(del, responseType);
            }
        }
    }

    private void validateDeliveryConsistency(DeliveryTmp delivery, ResponseType type) {
        if (delivery.getText() == null || delivery.getText().isBlank()) {
            throw new IllegalArgumentException("Delivery can't be empty");
        }

        if (type == ResponseType.FLAG) {
            if (delivery.getSolution() == null || delivery.getSolution().isBlank()) {
                throw new IllegalArgumentException("For a FLAG Hackathon, each submission must include the expected solution.");
            }
        }
    }
}