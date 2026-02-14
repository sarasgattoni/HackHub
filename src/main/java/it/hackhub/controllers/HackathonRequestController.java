package it.hackhub.controllers;

import it.hackhub.dtos.CreateHackathonRequest;
import it.hackhub.service.HackathonRequestHandler;

public class HackathonRequestController {

    private final HackathonRequestHandler handler = new HackathonRequestHandler();

    public String createHackathonRequest(CreateHackathonRequest request) {
        try {
            handler.submitHackathonRequest(
                    request.getOrganizerId(),
                    request.getInfo(),
                    request.getExecutionPeriod(),
                    request.getSubscriptionPeriod(),
                    request.getRules(),
                    request.getResponseType(),
                    request.getDeliveries()
            );
            return "{\"status\": 200, \"message\": \"Request created successfully\"}";
        } catch (Exception e) {
            return "{\"status\": 400, \"error\": \"" + e.getMessage() + "\"}";
        }
    }

    public String approveRequest(Long id) {
        try {
            handler.approveRequest(id);
            return "{\"status\": 200, \"message\": \"Request approved\"}";
        } catch (Exception e) {
            return "{\"status\": 400, \"error\": \"" + e.getMessage() + "\"}";
        }
    }

    public String declineRequest(Long id, String reason) {
        try {
            handler.declineRequest(id, reason);
            return "{\"status\": 200, \"message\": \"Request declined\"}";
        } catch (Exception e) {
            return "{\"status\": 400, \"error\": \"" + e.getMessage() + "\"}";
        }
    }

    public String getPendingRequests() {
        try {
            var requests = handler.getPendingRequests();
            // Nota: Assicurati che HackathonRequest abbia un toString() valido (es. @Data o @ToString di Lombok)
            return "{\"status\": 200, \"data\": " + requests.toString() + "}";
        } catch (Exception e) {
            return "{\"status\": 400, \"error\": \"" + e.getMessage() + "\"}";
        }
    }
}