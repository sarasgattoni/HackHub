package it.hackhub.service.builder;

import it.hackhub.model.DeliveryTmp;
import it.hackhub.model.Hackathon;
import it.hackhub.model.enums.ResponseType;
import it.hackhub.model.valueobjs.Info;
import it.hackhub.model.valueobjs.Period;
import it.hackhub.model.valueobjs.Rules;

import java.util.List;

public interface IHackathonBuilder {

    void reset();

    void buildGeneralInfo(Info infos);

    void buildExecutionDates(Period period);

    void buildSubscriptionDates(Period period);

    void buildRules(Rules rules);

    void buildResponseType(ResponseType responseType);

    void addDeliveries(List<DeliveryTmp> deliveries);

    Hackathon getResult();
}