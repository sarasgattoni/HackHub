package it.hackhub.controllers;

import it.hackhub.dtos.CreateHackathonRequest;
import it.hackhub.service.builder.HackathonRequestHandler;

public class HackathonRequestController {

    private final HackathonRequestHandler requestHandler;

    public HackathonRequestController() {
        this.requestHandler = new HackathonRequestHandler();
    }

    public String createRequest(CreateHackathonRequest requestDTO, Long staffId) {
        try {
            requestHandler.createHackathonRequest(requestDTO, staffId);
            return "{\"status\": 200, \"message\": \"Hackathon Request Created Successfully\"}";
        } catch (IllegalArgumentException | IllegalStateException e) {
            return "{\"status\": 400, \"error\": \"" + e.getMessage() + "\"}";
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"status\": 500, \"error\": \"Internal Server Error\"}";
        }
    }
}