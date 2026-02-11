package it.hackhub.dtos;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CreateHackathonRequest {
    private String name;
    private String type;
    private String prize;
    private boolean isOnline;
    private String location;

    private LocalDate executionStartDate;
    private LocalDate executionEndDate;
    private LocalDate subscriptionStartDate;
    private LocalDate subscriptionEndDate;

    private int maxTeamSize;
    private String ruleDocument;

    private String responseType;

    private List<DeliveryDTO> deliveries;
}