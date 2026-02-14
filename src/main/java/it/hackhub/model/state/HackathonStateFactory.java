package it.hackhub.model.state;

public class HackathonStateFactory {
    public static IHackathonState getFromName(String name) {
        if (name == null) return new PendingState();
        switch (name) {
            case "PENDING": return new PendingState();
            case "APPROVED": return new ApprovedState();
            case "SUBSCRIPTION_OPEN": return new SubscriptionOpenState();
            case "IN_EXECUTION": return new InExecutionState();
            case "CLOSED": return new ClosedState();
            case "ARCHIVED": return new ArchivedState();
            default: throw new IllegalArgumentException("Unknown State: " + name);
        }
    }
}