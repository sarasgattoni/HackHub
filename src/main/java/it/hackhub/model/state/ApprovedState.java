package it.hackhub.model.state;

import it.hackhub.model.Hackathon;
import it.hackhub.model.StaffAssignment;
import it.hackhub.model.accounts.StaffProfile;
import it.hackhub.model.enums.StaffRole;

public class ApprovedState implements IHackathonState {

    @Override
    public String getName() { return "APPROVED"; }

    @Override
    public void next(Hackathon h) {
        h.setState(new SubscriptionOpenState());
    }

    @Override
    public void recruitStaff(Hackathon h, StaffProfile profile, StaffRole role) {
        StaffAssignment assignment = new StaffAssignment(profile, h, role);
        h.addStaffAssignment(assignment);
    }
}