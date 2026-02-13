package it.hackhub.dtos;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateHackathonRequest {

    private String name;
    private String type;
    private String location;
    private double prize;
    private boolean isOnline;

    private int maxTeamSize;
    private String ruleDocument;

    private LocalDateTime executionStartDate;
    private LocalDateTime executionEndDate;
    private LocalDateTime subscriptionStartDate;
    private LocalDateTime subscriptionEndDate;

    private String responseType;

    private List<DeliveryDTO> deliveries;
}