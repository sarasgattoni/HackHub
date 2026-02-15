package it.hackhub.model.state;

import it.hackhub.model.*;
import it.hackhub.model.accounts.StaffProfile;
import it.hackhub.model.valueobjs.GitHubUrl;
import java.util.List;

public class InExecutionState implements IHackathonState {

    @Override
    public String getName() { return "IN_EXECUTION"; }

    @Override
    public void next(Hackathon h) {
        h.setState(new ClosedState());
    }

    @Override
    public void submitSolution(Hackathon h, ParticipatingTeam team, Delivery delivery, String solutionUrl) {
        if (!h.getDeliveries().contains(delivery)) {
            throw new IllegalArgumentException("Delivery non valida per questo Hackathon");
        }
        Submission sub = new Submission(team, delivery);
        sub.update(new GitHubUrl(solutionUrl));
    }

    @Override
    public List<String> viewDeliveries(Hackathon h, StaffProfile requestor) {
        return h.getDeliveriesTextList();
    }
}