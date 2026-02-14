package it.hackhub.model.state;

import it.hackhub.model.*;
import it.hackhub.model.enums.StaffRole;
import java.io.Serializable;
import java.util.List;

public interface IHackathonState extends Serializable {

    String getName();
    void next(Hackathon hackathon);

    default void recruitStaff(Hackathon h, StaffProfile profile, StaffRole role) {
        throw new IllegalStateException("Non è possibile reclutare staff nello stato " + getName());
    }

    default void enrollTeam(Hackathon h, Team team) {
        throw new IllegalStateException("Le iscrizioni sono chiuse nello stato " + getName());
    }

    default void submitSolution(Hackathon h, ParticipatingTeam team, Delivery delivery, String solutionUrl) {
        throw new IllegalStateException("Non puoi inviare soluzioni nello stato " + getName());
    }

    default void evaluateSubmission(Hackathon h, Submission s, int score, String comment, StaffProfile judge) {
        throw new IllegalStateException("La valutazione è chiusa nello stato " + getName());
    }

    default void publishRanking(Hackathon h) {
        throw new IllegalStateException("Non puoi pubblicare la classifica nello stato " + getName());
    }

    default List<String> viewDeliveries(Hackathon h, StaffProfile requestor) {
        throw new IllegalStateException("Impossibile vedere le consegne nello stato " + getName());
    }
}