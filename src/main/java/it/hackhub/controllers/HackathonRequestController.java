package it.hackhub.controllers;

import it.hackhub.dtos.CreateHackathonRequest;
import it.hackhub.dtos.DeliveryDTO;
import it.hackhub.model.DeliveryTmp;
import it.hackhub.model.enums.ResponseType;
import it.hackhub.model.valueobjs.Info;
import it.hackhub.model.valueobjs.Period;
import it.hackhub.model.valueobjs.Rules;
import it.hackhub.service.HackathonRequestHandler;

import java.util.ArrayList;
import java.util.List;

public class HackathonRequestController {

    private final HackathonRequestHandler requestHandler;

    public HackathonRequestController() {
        this.requestHandler = new HackathonRequestHandler();
    }

    public String createRequest(CreateHackathonRequest requestDTO, Long staffId) {
        try {
            Info info = new Info(
                    requestDTO.getName(),
                    requestDTO.getType(),
                    requestDTO.getLocation(),
                    requestDTO.getPrize(),
                    requestDTO.isOnline()
            );

            Rules rules = new Rules(requestDTO.getMaxTeamSize(), requestDTO.getRuleDocument());

            Period executionPeriod = new Period(requestDTO.getExecutionStartDate(), requestDTO.getExecutionEndDate());
            Period subscriptionPeriod = new Period(requestDTO.getSubscriptionStartDate(), requestDTO.getSubscriptionEndDate());

            ResponseType responseType;
            try {
                responseType = ResponseType.valueOf(requestDTO.getResponseType());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid response type. Allowed values: FLAG, SOFTWARE");
            }

            List<DeliveryTmp> deliveries = new ArrayList<>();
            if (requestDTO.getDeliveries() != null) {
                for (DeliveryDTO delDto : requestDTO.getDeliveries()) {
                    deliveries.add(new DeliveryTmp(
                            delDto.getText(),
                            delDto.getAttachmentUrl(),
                            delDto.getSolution()
                    ));
                }
            }

            requestHandler.submitHackathonRequest(
                    staffId,
                    info,
                    executionPeriod,
                    subscriptionPeriod,
                    rules,
                    responseType,
                    deliveries
            );

            return "{\"status\": 200, \"message\": \"Hackathon Request Created Successfully\"}";

        } catch (IllegalArgumentException | IllegalStateException e) {
            return "{\"status\": 400, \"error\": \"" + e.getMessage() + "\"}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"status\": 500, \"error\": \"Internal Server Error\"}";
        }
    }
}