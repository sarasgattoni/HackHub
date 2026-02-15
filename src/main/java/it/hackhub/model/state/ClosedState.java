package it.hackhub.model.state;

import it.hackhub.model.Hackathon;
import it.hackhub.model.accounts.StaffProfile;
import it.hackhub.model.Submission;
import java.util.List;

public class ClosedState implements IHackathonState {

    @Override
    public String getName() { return "CLOSED"; }

    @Override
    public void next(Hackathon h) {
        h.setState(new ArchivedState());
    }

    @Override
    public void evaluateSubmission(Hackathon h, Submission s, int score, String comment, StaffProfile judge) {
        s.evaluate(score, comment, judge);
    }

    @Override
    public void publishRanking(Hackathon h) {
    }

    @Override
    public List<String> viewDeliveries(Hackathon h, StaffProfile requestor) {
        return h.getDeliveriesTextList();
    }
}