package it.hackhub.controllers;

import it.hackhub.service.UserHandler;


public class TeamController {
    private final UserHandler userHandler = new UserHandler();

    public String inviteUser(Long recipientId, Long senderId) {
        try {
            userHandler.inviteUserToTeam(recipientId, senderId);
            return "{\"status\": 200, \"message\": \"Invitation sent\"}";
        } catch (Exception e) {
            return "{\"status\": 400, \"error\": \"" + e.getMessage() + "\"}";
        }
    }
}