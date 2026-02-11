package it.hackhub.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hackathons")
@Getter @Setter
@NoArgsConstructor
public class Hackathon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
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

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Delivery> deliveries = new ArrayList<>();

    public void addDelivery(Delivery delivery) {
        deliveries.add(delivery);
        delivery.setHackathon(this);
    }

    public boolean isInExecution() {
        if (this.executionStartDate == null || this.executionEndDate == null) return false;

        java.time.LocalDate now = java.time.LocalDate.now();
        return !now.isBefore(this.executionStartDate) && !now.isAfter(this.executionEndDate);
    }

    public void assertIsOpenForSubscription() {
        if (this.subscriptionStartDate == null || this.subscriptionEndDate == null) {
            throw new IllegalStateException("Subscription dates haven't been set");
        }

        java.time.LocalDate now = java.time.LocalDate.now();
        if (now.isBefore(this.subscriptionStartDate) || now.isAfter(this.subscriptionEndDate)) {
            throw new IllegalStateException("Hackathon is currently closed to subscriptions");
        }
    }

    public void assertApprovedOrSubscription() {
        if (this.executionStartDate != null && java.time.LocalDate.now().isAfter(this.executionStartDate)) {
            throw new IllegalStateException("Impossible to change roles, hackathon has already started");
        }
    }
}