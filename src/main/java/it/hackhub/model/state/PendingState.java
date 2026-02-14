package it.hackhub.model.state;

import it.hackhub.model.Hackathon;
import it.hackhub.model.StaffAssignment;
import it.hackhub.model.StaffProfile;
import it.hackhub.model.enums.StaffRole;

public class PendingState implements IHackathonState {

    @Override
    public String getName() { return "PENDING"; }

    @Override
    public void next(Hackathon h) {
        h.setState(new ApprovedState());
    }

    @Override
    public void recruitStaff(Hackathon h, StaffProfile profile, StaffRole role) {
        StaffAssignment assignment = new StaffAssignment(profile, h, role);
        h.addStaffAssignment(assignment);
    }
}