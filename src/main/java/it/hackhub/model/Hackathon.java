package it.hackhub.model;

import it.hackhub.model.enums.*;
import it.hackhub.model.state.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "hackathons")
@Getter
@Setter
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

    @Column(columnDefinition = "TEXT")
    private String ruleDocument;
    private String responseType;

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StaffAssignment> staffAssignments = new ArrayList<>();

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Delivery> deliveries = new ArrayList<>();

    @OneToMany(mappedBy = "hackathon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticipatingTeam> participatingTeams = new ArrayList<>();

    @Column(name = "state", nullable = false)
    @Setter(AccessLevel.NONE)
    private String stateName;

    @Transient
    private IHackathonState state;

    public Hackathon() {
        this.setState(new PendingState());
    }

    @PostLoad
    private void initAfterLoad() {
        this.state = HackathonStateFactory.getFromName(this.stateName);
    }

    public void setState(IHackathonState newState) {
        this.state = newState;
        this.stateName = newState.getName();
    }

    public void nextState() {
        this.state.next(this);
    }

    public boolean isOrganizer(StaffProfile profile) {
        return hasRole(profile, StaffRole.ORGANIZER);
    }

    public boolean isJudge(StaffProfile profile) {
        return hasRole(profile, StaffRole.JUDGE);
    }

    public boolean isMentor(StaffProfile profile) {
        return hasRole(profile, StaffRole.MENTOR);
    }

    public boolean isStaff(StaffProfile profile) {
        if (profile == null) return false;
        return staffAssignments.stream()
                .anyMatch(sa -> sa.getStaffProfile().getId().equals(profile.getId()));
    }

    private boolean hasRole(StaffProfile profile, StaffRole role) {
        if (profile == null) return false;
        return staffAssignments.stream()
                .anyMatch(sa -> sa.getStaffProfile().getId().equals(profile.getId())
                        && sa.getRole() == role);
    }

    public void recruitStaff(StaffProfile actor, StaffProfile newMember, StaffRole role) {
        if (!isOrganizer(actor)) {
            throw new SecurityException("Only Organizer can recruit staff");
        }
        this.state.recruitStaff(this, newMember, role);
    }

    public void enrollTeam(Team team) {
        this.state.enrollTeam(this, team);
    }

    public void submitSolution(StaffProfile actor, ParticipatingTeam team, Delivery delivery, String solutionUrl) {
        this.state.submitSolution(this, team, delivery, solutionUrl);
    }

    public void evaluateSubmission(StaffProfile actor, Submission submission, int score, String comment) {
        if (!isJudge(actor)) {
            throw new SecurityException("Only Judges can evaluate submissions");
        }
        this.state.evaluateSubmission(this, submission, score, comment, actor);
    }

    public void publishRanking(StaffProfile actor) {
        if (!isOrganizer(actor)) {
            throw new SecurityException("Only Organizer can publish ranking");
        }
        this.state.publishRanking(this);
    }

    public List<String> viewDeliveries(StaffProfile requestor) {
        return this.state.viewDeliveries(this, requestor);
    }

    public void addStaffAssignment(StaffAssignment assignment) {
        this.staffAssignments.add(assignment);
    }

    public void addDelivery(Delivery delivery) {
        this.deliveries.add(delivery);
        delivery.setHackathon(this);
    }

    public void addParticipatingTeam(ParticipatingTeam pt) {
        this.participatingTeams.add(pt);
    }

    public List<String> getDeliveriesTextList() {
        return deliveries.stream().map(Delivery::getText).collect(Collectors.toList());
    }
}