package it.hackhub.model.state;

import it.hackhub.model.Hackathon;
import it.hackhub.model.StaffProfile;
import java.util.List;

public class ArchivedState implements IHackathonState {

    @Override
    public String getName() { return "ARCHIVED"; }

    @Override
    public void next(Hackathon h) {
        throw new IllegalStateException("Hackathon archiviato.");
    }

    @Override
    public List<String> viewDeliveries(Hackathon h, StaffProfile requestor) {
        return h.getDeliveriesTextList();
    }
}