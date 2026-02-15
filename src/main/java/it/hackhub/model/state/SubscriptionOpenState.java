package it.hackhub.model.state;

import it.hackhub.model.Hackathon;
import it.hackhub.model.ParticipatingTeam;
import it.hackhub.model.accounts.StaffProfile;
import it.hackhub.model.Team;
import java.util.List;

public class SubscriptionOpenState implements IHackathonState {

    @Override
    public String getName() { return "SUBSCRIPTION_OPEN"; }

    @Override
    public void next(Hackathon h) {
        h.setState(new InExecutionState());
    }

    @Override
    public void enrollTeam(Hackathon h, Team team) {
        if (team.getSize() > h.getMaxTeamSize()) {
            throw new IllegalArgumentException("Team size exceeds limit");
        }
        ParticipatingTeam pt = new ParticipatingTeam(team, h);
        h.addParticipatingTeam(pt);
    }

    @Override
    public List<String> viewDeliveries(Hackathon h, StaffProfile requestor) {
        if (h.isStaff(requestor)) {
            return h.getDeliveriesTextList();
        }
        throw new SecurityException("Deliveries are restricted to Staff in this state");
    }
}