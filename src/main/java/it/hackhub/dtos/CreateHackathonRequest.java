package it.hackhub.dtos;

import it.hackhub.model.DeliveryTmp;
import it.hackhub.model.enums.ResponseType;
import it.hackhub.model.valueobjs.Info;
import it.hackhub.model.valueobjs.Period;
import it.hackhub.model.valueobjs.Rules;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateHackathonRequest {
    private Long organizerId;
    private Info info;
    private Period executionPeriod;
    private Period subscriptionPeriod;
    private Rules rules;
    private ResponseType responseType;

    private List<DeliveryTmp> deliveries;
}