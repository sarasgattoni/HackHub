package it.hackhub.controllers;

import it.hackhub.service.SubmissionHandler;

public class SubmissionController {
    private final SubmissionHandler submissionHandler = new SubmissionHandler();

    public String viewDeliveries(Long hackathonId, Long userId) {
        try {
            var deliveries = submissionHandler.getHackathonDeliveries(hackathonId, userId);
            return "{\"status\": 200, \"data\": " + deliveries.toString() + "}";
        } catch (Exception e) {
            return "{\"status\": 400, \"error\": \"" + e.getMessage() + "\"}";
        }
    }
}