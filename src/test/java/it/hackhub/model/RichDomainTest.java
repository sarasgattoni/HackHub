package it.hackhub.model;

import it.hackhub.model.accounts.StaffProfile;
import it.hackhub.model.accounts.User;
import it.hackhub.model.enums.StaffRole;
import it.hackhub.model.state.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RichDomainTest {

    private Hackathon hackathon;
    private StaffProfile organizer;
    private StaffProfile judge;
    private StaffProfile randomUser;
    private Team team;

    @BeforeEach
    void setUp() {
        hackathon = new Hackathon();
        hackathon.setName("Test Hackathon");
        hackathon.setMaxTeamSize(5);

        organizer = new StaffProfile();
        organizer.setId(1L);

        judge = new StaffProfile();
        judge.setId(2L);

        randomUser = new StaffProfile();
        randomUser.setId(99L);

        team = new Team();
        team.setName("Super Team");

        User leader = new User();
        leader.setId(10L);
        team.getMembers().add(leader);
        team.setLeader(leader);
    }

    @Nested
    class StateTransitionTests {

        @Test
        void shouldStartInPendingState() {
            assertEquals("PENDING", hackathon.getStateName());
            assertTrue(hackathon.getState() instanceof PendingState);
        }

        @Test
        void shouldTransitionThroughLifecycle() {
            hackathon.nextState();
            assertEquals("APPROVED", hackathon.getStateName());

            hackathon.nextState();
            assertEquals("SUBSCRIPTION_OPEN", hackathon.getStateName());

            hackathon.nextState();
            assertEquals("IN_EXECUTION", hackathon.getStateName());

            hackathon.nextState();
            assertEquals("CLOSED", hackathon.getStateName());

            hackathon.nextState();
            assertEquals("ARCHIVED", hackathon.getStateName());
        }

        @Test
        void shouldThrowExceptionWhenArchived() {
            hackathon.setState(new ArchivedState());
            assertThrows(IllegalStateException.class, hackathon::nextState);
        }
    }

    @Nested
    class StaffRecruitmentTests {

        @Test
        void shouldAllowOrganizerToRecruitInPending() {
            hackathon.addStaffAssignment(new StaffAssignment(organizer, hackathon, StaffRole.ORGANIZER));

            hackathon.recruitStaff(organizer, judge, StaffRole.JUDGE);

            assertTrue(hackathon.isJudge(judge));
        }

        @Test
        void shouldBlockRecruitmentInClosedState() {
            hackathon.addStaffAssignment(new StaffAssignment(organizer, hackathon, StaffRole.ORGANIZER));
            hackathon.setState(new ClosedState());

            assertThrows(IllegalStateException.class, () ->
                    hackathon.recruitStaff(organizer, judge, StaffRole.JUDGE));
        }

        @Test
        void shouldBlockUnauthorizedRecruiter() {
            assertThrows(SecurityException.class, () ->
                    hackathon.recruitStaff(randomUser, judge, StaffRole.JUDGE));
        }
    }

    @Nested
    class EnrollmentTests {

        @Test
        void shouldAllowEnrollmentInSubscriptionOpen() {
            hackathon.setState(new SubscriptionOpenState());

            hackathon.enrollTeam(team);

            assertEquals(1, hackathon.getParticipatingTeams().size());
            assertEquals("Super Team", hackathon.getParticipatingTeams().get(0).getTeam().getName());
        }

        @Test
        void shouldBlockEnrollmentInPending() {
            assertThrows(IllegalStateException.class, () -> hackathon.enrollTeam(team));
        }

        @Test
        void shouldThrowIfTeamTooBig() {
            hackathon.setState(new SubscriptionOpenState());
            hackathon.setMaxTeamSize(1);

            team.getMembers().add(new User());
            team.getMembers().add(new User());

            assertThrows(IllegalArgumentException.class, () -> hackathon.enrollTeam(team));
        }
    }

    @Nested
    class ExecutionTests {
        private ParticipatingTeam pTeam;
        private Delivery delivery;

        @BeforeEach
        void setupExecution() {
            hackathon.setState(new InExecutionState());
            pTeam = new ParticipatingTeam(team, hackathon);
            hackathon.addParticipatingTeam(pTeam);

            delivery = new StandardDelivery();
            hackathon.addDelivery(delivery);

            hackathon.addStaffAssignment(new StaffAssignment(organizer, hackathon, StaffRole.ORGANIZER));
        }

        @Test
        void shouldAllowSubmissionInExecution() {
            hackathon.submitSolution(organizer, pTeam, delivery, "https://github.com/myuser/myrepo");
        }

        @Test
        void shouldBlockSubmissionIfClosed() {
            hackathon.setState(new ClosedState());
            assertThrows(IllegalStateException.class, () ->
                    hackathon.submitSolution(organizer, pTeam, delivery, "https://github.com/myuser/myrepo"));
        }
    }
}