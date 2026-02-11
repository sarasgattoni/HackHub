package it.hackhub.model.enums;

public enum HackathonState {
    PENDING,
    APPROVED,
    SUBSCRIPTION_OPEN,
    IN_EXECUTION,
    CLOSED,
    ARCHIVED;

    public boolean allows(HackathonAction action) {
        switch (this) {
            case PENDING:
            case APPROVED:
                return action == HackathonAction.MANAGE_STAFF;

            case SUBSCRIPTION_OPEN:
                return action == HackathonAction.VIEW_DETAILS;

            case IN_EXECUTION:
                return action == HackathonAction.VIEW_DETAILS ||
                       action == HackathonAction.SUBMIT_SOLUTION;

            case CLOSED:
                return action == HackathonAction.VIEW_SUBMISSIONS ||
                       action == HackathonAction.EVALUATE_SUBMISSIONS ||
                       action == HackathonAction.PUBLISH_RANKING;

            case ARCHIVED:
                return action == HackathonAction.VIEW_DETAILS ||
                       action == HackathonAction.VIEW_SUBMISSIONS;

            default: return false;
        }
    }
}
