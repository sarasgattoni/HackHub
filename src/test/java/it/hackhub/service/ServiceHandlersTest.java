package it.hackhub.service;

import it.hackhub.model.*;
import it.hackhub.model.enums.RequestState;
import it.hackhub.model.enums.StaffRole;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.model.valueobjs.Email;
import it.hackhub.model.valueobjs.GitHubUrl;
import it.hackhub.model.valueobjs.UserPassword;
import it.hackhub.repository.*;
import org.hibernate.Session;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test Suite: Handlers (Service Layer Orchestration)")
class ServiceHandlersTest {

    @Mock
    private Session mockSession;

    private MockedStatic<HibernateExecutor> mockedExecutor;

    private User dummyLeader;
    private User dummyMember;
    private Team dummyTeam;
    private Hackathon dummyHackathon;
    private StaffProfile dummyOrg;
    private Admin dummyAdmin;

    @BeforeEach
    void globalSetup() {
        mockedExecutor = mockStatic(HibernateExecutor.class);

        mockedExecutor.when(() -> HibernateExecutor.executeVoidTransaction(any())).thenAnswer(inv -> {
            HibernateExecutor.HibernateConsumer consumer = inv.getArgument(0);
            consumer.accept(mockSession);
            return null;
        });

        mockedExecutor.when(() -> HibernateExecutor.execute(any())).thenAnswer(inv -> {
            HibernateExecutor.HibernateFunction func = inv.getArgument(0);
            return func.apply(mockSession);
        });

        UserPassword pwd = new UserPassword("Password123!");
        dummyLeader = new User("Leader", new Email("l@h.it"), pwd);
        setEntityId(dummyLeader, 1L);

        dummyMember = new User("Member", new Email("m@h.it"), pwd);
        setEntityId(dummyMember, 2L);

        dummyTeam = new Team("Alpha", dummyLeader);
        setEntityId(dummyTeam, 10L);

        dummyHackathon = new Hackathon();
        setEntityId(dummyHackathon, 100L);
        dummyHackathon.setMaxTeamSize(4);

        dummyOrg = new StaffProfile("Org", "Admin", new Email("org@h.it"), pwd, StaffRole.ORGANIZER);
        setEntityId(dummyOrg, 5L);

        dummyAdmin = new Admin(new Email("admin@h.it"), pwd);
        setEntityId(dummyAdmin, 999L);
    }

    @AfterEach
    void globalTeardown() {
        mockedExecutor.close();
    }

    @Nested
    @DisplayName("1. JoinHackathonHandler Tests")
    class JoinHackathonHandlerTests {
        private MockedConstruction<HackathonRepository> mockHackRepo;
        private MockedConstruction<UserRepository> mockUserRepo;
        private MockedConstruction<TeamRepository> mockTeamRepo;
        private MockedConstruction<ParticipatingTeamRepository> mockPartTeamRepo;
        private JoinHackathonHandler handler;

        @BeforeEach
        void setup() {
            mockHackRepo = mockConstruction(HackathonRepository.class);
            mockUserRepo = mockConstruction(UserRepository.class);
            mockTeamRepo = mockConstruction(TeamRepository.class);
            mockPartTeamRepo = mockConstruction(ParticipatingTeamRepository.class);
            handler = new JoinHackathonHandler();
        }

        @AfterEach
        void teardown() {
            mockHackRepo.close(); mockUserRepo.close(); mockTeamRepo.close(); mockPartTeamRepo.close();
        }

        @Test
        @DisplayName("Successo: Il leader iscrive il team all'Hackathon aperto")
        void testJoinSuccess() {
            dummyHackathon.setSubscriptionStartDate(LocalDate.now().minusDays(1));
            dummyHackathon.setSubscriptionEndDate(LocalDate.now().plusDays(1));

            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));
            when(mockUserRepo.constructed().get(0).findById(mockSession, 1L)).thenReturn(Optional.of(dummyLeader));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 1L)).thenReturn(Optional.of(dummyTeam));
            when(mockPartTeamRepo.constructed().get(0).findByTeamAndHackathon(mockSession, 10L, 100L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> handler.joinHackathon(1L, 100L));
            verify(mockPartTeamRepo.constructed().get(0), times(1)).save(eq(mockSession), any(ParticipatingTeam.class));
        }

        @Test
        @DisplayName("Limite: Iscrizione fallisce se l'Hackathon non esiste")
        void testJoinHackathonNotFound() {
            when(mockHackRepo.constructed().get(0).findById(mockSession, 999L)).thenReturn(Optional.empty());
            assertThrows(IllegalArgumentException.class, () -> handler.joinHackathon(1L, 999L));
        }

        @Test
        @DisplayName("Limite: Iscrizione fallisce se l'Hackathon ha le iscrizioni chiuse")
        void testJoinHackathonClosed() {
            dummyHackathon.setSubscriptionStartDate(LocalDate.now().minusDays(10));
            dummyHackathon.setSubscriptionEndDate(LocalDate.now().minusDays(1));

            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));
            assertThrows(IllegalStateException.class, () -> handler.joinHackathon(1L, 100L));
        }

        @Test
        @DisplayName("Sicurezza: Iscrizione fallisce se l'utente non è il leader")
        void testJoinNotLeader() {
            dummyHackathon.setSubscriptionStartDate(LocalDate.now().minusDays(1));
            dummyHackathon.setSubscriptionEndDate(LocalDate.now().plusDays(1));

            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));
            when(mockUserRepo.constructed().get(0).findById(mockSession, 2L)).thenReturn(Optional.of(dummyMember));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 2L)).thenReturn(Optional.of(dummyTeam));

            assertThrows(IllegalStateException.class, () -> handler.joinHackathon(2L, 100L));
        }

        @Test
        @DisplayName("Business Rule: Iscrizione fallisce se il team supera la capienza massima")
        void testJoinTeamTooBig() {
            dummyHackathon.setSubscriptionStartDate(LocalDate.now().minusDays(1));
            dummyHackathon.setSubscriptionEndDate(LocalDate.now().plusDays(1));
            dummyHackathon.setMaxTeamSize(1);

            dummyTeam.addMember(dummyMember);

            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));
            when(mockUserRepo.constructed().get(0).findById(mockSession, 1L)).thenReturn(Optional.of(dummyLeader));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 1L)).thenReturn(Optional.of(dummyTeam));

            assertThrows(IllegalStateException.class, () -> handler.joinHackathon(1L, 100L));
        }

        @Test
        @DisplayName("Business Rule: Iscrizione fallisce se il team è già iscritto")
        void testJoinAlreadyParticipating() {
            dummyHackathon.setSubscriptionStartDate(LocalDate.now().minusDays(1));
            dummyHackathon.setSubscriptionEndDate(LocalDate.now().plusDays(1));

            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));
            when(mockUserRepo.constructed().get(0).findById(mockSession, 1L)).thenReturn(Optional.of(dummyLeader));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 1L)).thenReturn(Optional.of(dummyTeam));

            when(mockPartTeamRepo.constructed().get(0).findByTeamAndHackathon(mockSession, 10L, 100L)).thenReturn(Optional.of(new ParticipatingTeam()));

            assertThrows(IllegalStateException.class, () -> handler.joinHackathon(1L, 100L));
        }
    }

    @Nested
    @DisplayName("2. RoleHandler Tests (Nomine e Assegnazioni)")
    class RoleHandlerTests {
        private MockedConstruction<StaffProfileRepository> mockStaffRepo;
        private MockedConstruction<HackathonRepository> mockHackRepo;
        private MockedConstruction<RoleRequestRepository> mockRoleReqRepo;
        private MockedConstruction<StaffAssignmentRepository> mockAssignRepo;
        private RoleHandler handler;
        private StaffAssignment dummyAssignment;

        @BeforeEach
        void setup() {
            mockStaffRepo = mockConstruction(StaffProfileRepository.class);
            mockHackRepo = mockConstruction(HackathonRepository.class);
            mockRoleReqRepo = mockConstruction(RoleRequestRepository.class);
            mockAssignRepo = mockConstruction(StaffAssignmentRepository.class);
            handler = new RoleHandler();

            dummyAssignment = new StaffAssignment(dummyOrg, dummyHackathon, StaffRole.ORGANIZER);
        }

        @AfterEach
        void teardown() {
            mockStaffRepo.close(); mockHackRepo.close(); mockRoleReqRepo.close(); mockAssignRepo.close();
        }

        @Test
        @DisplayName("Successo: L'organizzatore invia una richiesta di ruolo (Mentore)")
        void testSendRoleRequestSuccess() {
            dummyHackathon.setExecutionStartDate(LocalDate.now().plusDays(5));
            StaffProfile target = new StaffProfile("Nuovo", "Mentore", new Email("new@h.it"), null, StaffRole.MENTOR);

            when(mockStaffRepo.constructed().get(0).findById(mockSession, 5L)).thenReturn(Optional.of(dummyOrg));
            when(mockStaffRepo.constructed().get(0).findByEmail(mockSession, "new@h.it")).thenReturn(Optional.of(target));
            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));

            when(mockAssignRepo.constructed().get(0).findByHackathonAndRole(mockSession, 100L, StaffRole.ORGANIZER))
                    .thenReturn(Optional.of(dummyAssignment));

            assertDoesNotThrow(() -> handler.sendRoleRequest(5L, "new@h.it", 100L, StaffRole.MENTOR));
            verify(mockRoleReqRepo.constructed().get(0), times(1)).save(eq(mockSession), any(RoleRequest.class));
        }

        @Test
        @DisplayName("Sicurezza: Invio fallisce se chi richiede NON è l'organizzatore")
        void testSendRoleRequestNotOrganizer() {
            StaffProfile target = new StaffProfile("Nuovo", "Mentore", new Email("new@h.it"), null, StaffRole.MENTOR);

            when(mockStaffRepo.constructed().get(0).findById(mockSession, 5L)).thenReturn(Optional.of(dummyOrg));
            when(mockStaffRepo.constructed().get(0).findByEmail(mockSession, "new@h.it")).thenReturn(Optional.of(target));
            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));

            when(mockAssignRepo.constructed().get(0).findByHackathonAndRole(mockSession, 100L, StaffRole.ORGANIZER))
                    .thenReturn(Optional.empty());

            assertThrows(IllegalAccessError.class, () -> handler.sendRoleRequest(5L, "new@h.it", 100L, StaffRole.MENTOR));
        }

        @Test
        @DisplayName("Stato Macchina: Valutazione Approva -> Sostituisce ruolo e salva")
        void testEvaluateRoleRequestApprove() {
            RoleRequest req = new RoleRequest(dummyHackathon, dummyOrg, StaffRole.JUDGE);
            setEntityId(req, 77L);

            when(mockRoleReqRepo.constructed().get(0).findById(mockSession, 77L)).thenReturn(Optional.of(req));
            when(mockAssignRepo.constructed().get(0).findByHackathonAndRole(mockSession, 100L, StaffRole.JUDGE))
                    .thenReturn(Optional.empty());

            handler.evaluateRoleRequest(77L, true);

            assertEquals(RequestState.APPROVED, req.getState());
            verify(mockAssignRepo.constructed().get(0), times(1)).save(eq(mockSession), any(StaffAssignment.class));
            verify(mockRoleReqRepo.constructed().get(0), times(1)).save(mockSession, req);
        }
    }

    @Nested
    @DisplayName("3. SubmissionHandler Tests (GitHub URL)")
    class SubmissionHandlerTests {
        private MockedConstruction<HackathonRepository> mockHackRepo;
        private MockedConstruction<UserRepository> mockUserRepo;
        private MockedConstruction<TeamRepository> mockTeamRepo;
        private MockedConstruction<ParticipatingTeamRepository> mockPartTeamRepo;
        private MockedConstruction<DeliveryRepository> mockDelivRepo;
        private MockedConstruction<SubmissionRepository> mockSubRepo;
        private MockedConstruction<StaffProfileRepository> mockStaffRepo;
        private MockedConstruction<StaffAssignmentRepository> mockAssignRepo;
        private SubmissionHandler handler;

        private Delivery dummyDelivery;
        private ParticipatingTeam dummyPT;
        private final String validUrl = "https://github.com/team/repo";

        @BeforeEach
        void setup() {
            mockHackRepo = mockConstruction(HackathonRepository.class);
            mockUserRepo = mockConstruction(UserRepository.class);
            mockTeamRepo = mockConstruction(TeamRepository.class);
            mockPartTeamRepo = mockConstruction(ParticipatingTeamRepository.class);
            mockDelivRepo = mockConstruction(DeliveryRepository.class);
            mockSubRepo = mockConstruction(SubmissionRepository.class);
            mockStaffRepo = mockConstruction(StaffProfileRepository.class);
            mockAssignRepo = mockConstruction(StaffAssignmentRepository.class);
            handler = new SubmissionHandler();

            dummyDelivery = new StandardDelivery("Traccia 1");
            setEntityId(dummyDelivery, 50L);
            dummyPT = new ParticipatingTeam(dummyTeam, dummyHackathon);
            setEntityId(dummyPT, 88L);
        }

        @AfterEach
        void teardown() {
            mockHackRepo.close(); mockUserRepo.close(); mockTeamRepo.close(); mockPartTeamRepo.close();
            mockDelivRepo.close(); mockSubRepo.close(); mockStaffRepo.close(); mockAssignRepo.close();
        }

        @Test
        @DisplayName("Invio: Nuova sottomissione con URL GitHub Valido")
        void testSendSubmissionNew() {
            dummyHackathon.setExecutionStartDate(LocalDate.now());
            dummyHackathon.setExecutionEndDate(LocalDate.now());

            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));
            when(mockUserRepo.constructed().get(0).findById(mockSession, 1L)).thenReturn(Optional.of(dummyLeader));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 1L)).thenReturn(Optional.of(dummyTeam));
            when(mockPartTeamRepo.constructed().get(0).findByTeamAndHackathon(mockSession, 10L, 100L)).thenReturn(Optional.of(dummyPT));
            when(mockDelivRepo.constructed().get(0).findById(mockSession, 50L)).thenReturn(Optional.of(dummyDelivery));

            when(mockSubRepo.constructed().get(0).getByDeliveryAndTeam(mockSession, 50L, 88L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> handler.sendSubmission(100L, 50L, 1L, validUrl));

            verify(mockSubRepo.constructed().get(0), times(1)).save(eq(mockSession), any(Submission.class));
        }

        @Test
        @DisplayName("Invio: Fallisce se la stringa non è un URL GitHub")
        void testSendSubmissionInvalidUrl() {
            dummyHackathon.setExecutionStartDate(LocalDate.now());
            dummyHackathon.setExecutionEndDate(LocalDate.now());

            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));
            when(mockUserRepo.constructed().get(0).findById(mockSession, 1L)).thenReturn(Optional.of(dummyLeader));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 1L)).thenReturn(Optional.of(dummyTeam));
            when(mockPartTeamRepo.constructed().get(0).findByTeamAndHackathon(mockSession, 10L, 100L)).thenReturn(Optional.of(dummyPT));
            when(mockDelivRepo.constructed().get(0).findById(mockSession, 50L)).thenReturn(Optional.of(dummyDelivery));

            // Questa stringa farà lanciare un'eccezione dal costruttore di GitHubUrl all'interno dell'handler
            assertThrows(IllegalArgumentException.class, () -> handler.sendSubmission(100L, 50L, 1L, "link-invalido"));
        }

        @Test
        @DisplayName("Invio: Aggiornamento URL GitHub (Upsert)")
        void testSendSubmissionUpdateExisting() {
            dummyHackathon.setExecutionStartDate(LocalDate.now());
            dummyHackathon.setExecutionEndDate(LocalDate.now());

            Submission existingSub = new Submission(dummyPT, dummyDelivery);
            GitHubUrl oldUrl = new GitHubUrl("https://github.com/old/repo");
            existingSub.update(oldUrl);

            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));
            when(mockUserRepo.constructed().get(0).findById(mockSession, 1L)).thenReturn(Optional.of(dummyLeader));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 1L)).thenReturn(Optional.of(dummyTeam));
            when(mockPartTeamRepo.constructed().get(0).findByTeamAndHackathon(mockSession, 10L, 100L)).thenReturn(Optional.of(dummyPT));
            when(mockDelivRepo.constructed().get(0).findById(mockSession, 50L)).thenReturn(Optional.of(dummyDelivery));

            when(mockSubRepo.constructed().get(0).getByDeliveryAndTeam(mockSession, 50L, 88L)).thenReturn(Optional.of(existingSub));

            handler.sendSubmission(100L, 50L, 1L, validUrl);

            assertEquals(validUrl, existingSub.getRepositoryUrl().getValue());
            verify(mockSubRepo.constructed().get(0), times(1)).save(mockSession, existingSub);
        }
    }

    @Nested
    @DisplayName("4. TeamHandler Tests (Creazione e Inviti)")
    class TeamHandlerTests {
        private MockedConstruction<UserRepository> mockUserRepo;
        private MockedConstruction<TeamRepository> mockTeamRepo;
        private MockedConstruction<JoinTeamRequestRepository> mockJoinReqRepo;
        private TeamHandler handler;

        @BeforeEach
        void setup() {
            mockUserRepo = mockConstruction(UserRepository.class);
            mockTeamRepo = mockConstruction(TeamRepository.class);
            mockJoinReqRepo = mockConstruction(JoinTeamRequestRepository.class);
            handler = new TeamHandler();
        }

        @AfterEach
        void teardown() {
            mockUserRepo.close(); mockTeamRepo.close(); mockJoinReqRepo.close();
        }

        @Test
        @DisplayName("Limiti: La creazione del team fallisce se il nome esiste già")
        void testCreateTeamNameExists() {
            when(mockUserRepo.constructed().get(0).findById(mockSession, 1L)).thenReturn(Optional.of(dummyLeader));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 1L)).thenReturn(Optional.empty());
            when(mockTeamRepo.constructed().get(0).existsByName(mockSession, "Alpha")).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> handler.createTeam(1L, "Alpha"));
        }

        @Test
        @DisplayName("Sicurezza: L'invito fallisce se inviato da un NON leader")
        void testInviteNotLeader() {
            User target = new User("Target", new Email("t@h.it"), new UserPassword("Pwd12345!"));

            when(mockUserRepo.constructed().get(0).findByUsername(mockSession, "Target")).thenReturn(Optional.of(target));
            when(mockUserRepo.constructed().get(0).findById(mockSession, 2L)).thenReturn(Optional.of(dummyMember));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 2L)).thenReturn(Optional.of(dummyTeam));

            assertThrows(IllegalStateException.class, () -> handler.inviteUserToTeam(2L, "Target"));
        }
    }

    @Nested
    @DisplayName("5. HackathonRequestHandler Tests (Approvazione Evento)")
    class HackathonRequestHandlerTests {
        private MockedConstruction<HackathonRequestRepository> mockReqRepo;
        private MockedConstruction<AdminRepository> mockAdminRepo;
        private MockedConstruction<StaffProfileRepository> mockStaffRepo;
        private MockedConstruction<HackathonRepository> mockHackRepo;
        private HackathonRequestHandler handler;

        @BeforeEach
        void setup() {
            mockReqRepo = mockConstruction(HackathonRequestRepository.class);
            mockAdminRepo = mockConstruction(AdminRepository.class);
            mockStaffRepo = mockConstruction(StaffProfileRepository.class);
            mockHackRepo = mockConstruction(HackathonRepository.class);
            handler = new HackathonRequestHandler();
        }

        @AfterEach
        void teardown() {
            mockReqRepo.close(); mockAdminRepo.close(); mockStaffRepo.close(); mockHackRepo.close();
        }

        @Test
        @DisplayName("Creazione: L'Organizzatore invia la proposta di Hackathon")
        void testSubmitRequest() {
            when(mockStaffRepo.constructed().get(0).findById(mockSession, 5L)).thenReturn(Optional.of(dummyOrg));

            assertDoesNotThrow(() -> handler.submitHackathonRequest(5L, dummyHackathon));

            verify(mockHackRepo.constructed().get(0), times(1)).save(mockSession, dummyHackathon);
            verify(mockReqRepo.constructed().get(0), times(1)).save(eq(mockSession), any(HackathonRequest.class));
        }

        @Test
        @DisplayName("Valutazione: L'Admin approva la richiesta")
        void testEvaluateApprove() {
            HackathonRequest req = new HackathonRequest(dummyHackathon, dummyOrg);
            setEntityId(req, 88L);

            when(mockAdminRepo.constructed().get(0).findById(mockSession, 999L)).thenReturn(Optional.of(dummyAdmin));
            when(mockReqRepo.constructed().get(0).findById(mockSession, 88L)).thenReturn(Optional.of(req));

            handler.evaluateHackathonRequest(999L, 88L, true);

            assertEquals(RequestState.APPROVED, req.getState());
            verify(mockReqRepo.constructed().get(0), times(1)).save(mockSession, req);
        }

        @Test
        @DisplayName("Sicurezza: Un utente NON Admin non può valutare")
        void testEvaluateNotAdmin() {
            when(mockAdminRepo.constructed().get(0).findById(mockSession, 1L)).thenReturn(Optional.empty()); // Utente 1 non è admin

            assertThrows(IllegalAccessError.class, () -> handler.evaluateHackathonRequest(1L, 88L, true));
        }
    }

    private void setEntityId(Object entity, Long id) {
        try {
            java.lang.reflect.Field idField = null;
            Class<?> currentClass = entity.getClass();

            while (currentClass != null && idField == null) {
                try {
                    idField = currentClass.getDeclaredField("id");
                } catch (NoSuchFieldException e) {
                    currentClass = currentClass.getSuperclass();
                }
            }

            if (idField == null) {
                fail("Impossibile trovare il campo 'id' nell'entità " + entity.getClass().getSimpleName());
            }

            idField.setAccessible(true);
            idField.set(entity, id);

        } catch (Exception e) {
            throw new RuntimeException("Errore durante il setting dell'ID via Reflection", e);
        }
    }
}