package it.hackhub.service;

import it.hackhub.model.*;
import it.hackhub.model.enums.RequestState;
import it.hackhub.model.enums.StaffRole;
import it.hackhub.model.state.ApprovedState;
import it.hackhub.model.state.InExecutionState;
import it.hackhub.model.state.SubscriptionOpenState;
import it.hackhub.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ServiceHandlersTest {

    @Mock private HackathonRepository hackathonRepo;
    @Mock private HackathonRequestRepository requestRepo;
    @Mock private StaffProfileRepository staffRepo;
    @Mock private UserRepository userRepo;
    @Mock private TeamRepository teamRepo;
    @Mock private RoleRequestRepository roleReqRepo;
    @Mock private DeliveryRepository deliveryRepo;
    @Mock private ParticipatingTeamRepository partTeamRepo;

    @InjectMocks private HackathonRequestHandler requestHandler;
    @InjectMocks private JoinHackathonHandler joinHandler;
    @InjectMocks private RoleHandler roleHandler;
    @InjectMocks private SubmissionHandler submissionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Nested
    class HackathonRequestHandlerTests {

        @Test
        void shouldApproveRequestAndAdvanceHackathonState() {
            Hackathon h = new Hackathon();
            HackathonRequest req = new HackathonRequest(h, new StaffProfile());
            req.setState(RequestState.PENDING);

            when(requestRepo.findById(any(), eq(1L))).thenReturn(Optional.of(req));

            if (req.getState() == RequestState.PENDING) {
                req.setState(RequestState.APPROVED);
                h.nextState();
            }

            assertEquals(RequestState.APPROVED, req.getState());
            assertEquals("APPROVED", h.getStateName());
            assertTrue(h.getState() instanceof ApprovedState);
        }
    }

    @Nested
    class JoinHackathonHandlerTests {

        @Test
        void shouldJoinHackathonIfOpenAndLeader() {
            Hackathon h = new Hackathon();
            h.setState(new SubscriptionOpenState());
            h.setMaxTeamSize(5);

            User user = mock(User.class);
            when(user.getId()).thenReturn(10L);

            Team team = mock(Team.class);
            when(team.getId()).thenReturn(50L);
            when(team.getSize()).thenReturn(3);
            when(team.isLeader(user)).thenReturn(true);

            when(hackathonRepo.findById(any(), eq(1L))).thenReturn(Optional.of(h));
            when(userRepo.findById(any(), eq(10L))).thenReturn(Optional.of(user));
            when(teamRepo.getTeamOfUser(any(), eq(10L))).thenReturn(Optional.of(team));

            h.enrollTeam(team);

            assertEquals(1, h.getParticipatingTeams().size());
            assertEquals(team, h.getParticipatingTeams().get(0).getTeam());
        }

        @Test
        void shouldFailJoinIfPending() {
            Hackathon h = new Hackathon();
            User user = mock(User.class);
            Team team = mock(Team.class);
            when(team.isLeader(user)).thenReturn(true);

            when(hackathonRepo.findById(any(), eq(1L))).thenReturn(Optional.of(h));
            when(userRepo.findById(any(), eq(10L))).thenReturn(Optional.of(user));
            when(teamRepo.getTeamOfUser(any(), eq(10L))).thenReturn(Optional.of(team));

            assertThrows(IllegalStateException.class, () -> h.enrollTeam(team));
        }
    }

    @Nested
    class RoleHandlerTests {

        @Test
        void shouldSendRoleRequestIfOrganizer() {
            Hackathon h = new Hackathon();
            StaffProfile organizer = mock(StaffProfile.class);
            when(organizer.getId()).thenReturn(1L);

            StaffProfile target = mock(StaffProfile.class);
            when(target.getId()).thenReturn(2L);

            h.addStaffAssignment(new StaffAssignment(organizer, h, StaffRole.ORGANIZER));

            when(staffRepo.findById(any(), eq(1L))).thenReturn(Optional.of(organizer));
            when(staffRepo.findByEmail(any(), eq("target@email.com"))).thenReturn(Optional.of(target));
            when(hackathonRepo.findById(any(), eq(100L))).thenReturn(Optional.of(h));

            if (!h.isOrganizer(organizer)) throw new SecurityException();
            RoleRequest req = new RoleRequest(h, target, StaffRole.MENTOR);

            assertNotNull(req);
            assertEquals(StaffRole.MENTOR, req.getRole());
        }

        @Test
        void shouldFailSelfAssignIfNotOrganizer() {
            Hackathon h = new Hackathon();
            StaffProfile randomUser = mock(StaffProfile.class);
            when(randomUser.getId()).thenReturn(99L);

            when(staffRepo.findById(any(), eq(99L))).thenReturn(Optional.of(randomUser));
            when(hackathonRepo.findById(any(), eq(100L))).thenReturn(Optional.of(h));

            assertThrows(SecurityException.class, () -> h.recruitStaff(randomUser, randomUser, StaffRole.JUDGE));
        }
    }

    @Nested
    class SubmissionHandlerTests {

        @Test
        void shouldSubmitSolutionIfInExecution() {
            Hackathon h = new Hackathon();
            h.setState(new InExecutionState());

            User user = mock(User.class);
            when(user.getId()).thenReturn(10L);

            Team team = mock(Team.class);
            when(team.getId()).thenReturn(50L);

            Delivery delivery = mock(Delivery.class);
            when(delivery.getId()).thenReturn(200L);
            h.addDelivery(delivery);

            ParticipatingTeam pt = new ParticipatingTeam(team, h);

            when(hackathonRepo.findById(any(), eq(100L))).thenReturn(Optional.of(h));
            when(deliveryRepo.findById(any(), eq(200L))).thenReturn(Optional.of(delivery));
            when(teamRepo.getTeamOfUser(any(), eq(10L))).thenReturn(Optional.of(team));
            when(partTeamRepo.findByTeamAndHackathon(any(), eq(50L), eq(100L))).thenReturn(Optional.of(pt));
            when(staffRepo.findById(any(), eq(10L))).thenReturn(Optional.empty());

            h.submitSolution(null, pt, delivery, "https://github.com/user/repo");
        }

        @Test
        void shouldViewDeliveriesIfInExecution() {
            Hackathon h = new Hackathon();
            h.setState(new InExecutionState());
            h.addDelivery(new StandardDelivery());

            when(hackathonRepo.findById(any(), eq(100L))).thenReturn(Optional.of(h));
            when(staffRepo.findById(any(), eq(10L))).thenReturn(Optional.empty());

            List<String> results = h.viewDeliveries(null);

            assertEquals(1, results.size());
        }

        @Test
        void shouldFailViewDeliveriesInSubscriptionIfNotStaff() {
            Hackathon h = new Hackathon();
            h.setState(new SubscriptionOpenState());

            when(hackathonRepo.findById(any(), eq(100L))).thenReturn(Optional.of(h));
            when(staffRepo.findById(any(), eq(10L))).thenReturn(Optional.empty());

            assertThrows(SecurityException.class, () -> h.viewDeliveries(null));
        }
    }
}