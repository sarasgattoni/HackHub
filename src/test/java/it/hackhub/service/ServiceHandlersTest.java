package it.hackhub.service;

import it.hackhub.model.*;
import it.hackhub.model.enums.RequestState;
import it.hackhub.model.enums.StaffRole;
import it.hackhub.model.utils.HibernateExecutor;
import it.hackhub.model.valueobjs.Email;
import it.hackhub.model.valueobjs.UserPassword;
import it.hackhub.repository.*;
import org.hibernate.Session;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    // Entità fittizie ricorrenti
    private User dummyLeader;
    private User dummyMember;
    private Team dummyTeam;
    private Hackathon dummyHackathon;
    private StaffProfile dummyOrg;

    @BeforeEach
    void globalSetup() {
        // 1. Mockiamo l'esecuzione statica delle transazioni per eseguirle immediatamente passando il nostro mockSession
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

        // 2. Prepariamo i dati di base validi
        UserPassword pwd = new UserPassword("Password123!");
        dummyLeader = new User("Leader", new Email("l@h.it"), pwd);
        setEntityId(dummyLeader, 1L);

        dummyMember = new User("Member", new Email("m@h.it"), pwd);
        setEntityId(dummyMember, 2L);

        dummyTeam = new Team("Alpha", dummyLeader);
        setEntityId(dummyTeam, 10L);

        dummyHackathon = new Hackathon();
        dummyHackathon.setId(100L);
        setEntityId(dummyHackathon, 100L);

        dummyOrg = new StaffProfile("Org", "Admin", new Email("org@h.it"), pwd, StaffRole.ORGANIZER);
        setEntityId(dummyOrg, 5L);
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

            // Verifica che il salvataggio sia stato invocato una volta
            verify(mockPartTeamRepo.constructed().get(0), times(1)).save(eq(mockSession), any(ParticipatingTeam.class));
        }

        @Test
        @DisplayName("Limite: Iscrizione fallisce se l'Hackathon non esiste")
        void testJoinHackathonNotFound() {
            when(mockHackRepo.constructed().get(0).findById(mockSession, 999L)).thenReturn(Optional.empty());
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> handler.joinHackathon(1L, 999L));
            assertTrue(ex.getMessage().contains("non trovato"));
        }

        @Test
        @DisplayName("Limite: Iscrizione fallisce se l'Hackathon ha le iscrizioni chiuse")
        void testJoinHackathonClosed() {
            dummyHackathon.setSubscriptionStartDate(LocalDate.now().minusDays(10));
            dummyHackathon.setSubscriptionEndDate(LocalDate.now().minusDays(1)); // Chiuse ieri

            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));

            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> handler.joinHackathon(1L, 100L));
            assertTrue(ex.getMessage().contains("non è attualmente aperto"));
        }

        @Test
        @DisplayName("Sicurezza: Iscrizione fallisce se l'utente non è il leader")
        void testJoinNotLeader() {
            dummyHackathon.setSubscriptionStartDate(LocalDate.now().minusDays(1));
            dummyHackathon.setSubscriptionEndDate(LocalDate.now().plusDays(1));

            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));
            when(mockUserRepo.constructed().get(0).findById(mockSession, 2L)).thenReturn(Optional.of(dummyMember));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 2L)).thenReturn(Optional.of(dummyTeam)); // dummyTeam ha come leader dummyLeader, non dummyMember

            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> handler.joinHackathon(2L, 100L));
            assertTrue(ex.getMessage().contains("Solo il leader"));
        }

        @Test
        @DisplayName("Business Rule: Iscrizione fallisce se il team supera la capienza massima")
        void testJoinTeamTooBig() {
            dummyHackathon.setSubscriptionStartDate(LocalDate.now());
            dummyHackathon.setSubscriptionEndDate(LocalDate.now());
            dummyHackathon.setMaxTeamSize(1); // Capienza max 1

            dummyTeam.addMember(dummyMember); // Ora il team ha 2 membri (Leader + Member)

            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));
            when(mockUserRepo.constructed().get(0).findById(mockSession, 1L)).thenReturn(Optional.of(dummyLeader));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 1L)).thenReturn(Optional.of(dummyTeam));

            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> handler.joinHackathon(1L, 100L));
            assertTrue(ex.getMessage().contains("supera il limite massimo"));
        }

        @Test
        @DisplayName("Business Rule: Iscrizione fallisce se il team è già iscritto")
        void testJoinAlreadyParticipating() {
            dummyHackathon.setSubscriptionStartDate(LocalDate.now());
            dummyHackathon.setSubscriptionEndDate(LocalDate.now());

            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));
            when(mockUserRepo.constructed().get(0).findById(mockSession, 1L)).thenReturn(Optional.of(dummyLeader));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 1L)).thenReturn(Optional.of(dummyTeam));
            // Simuliamo che il team sia già iscritto
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
            dummyHackathon.setExecutionStartDate(LocalDate.now().plusDays(5)); // Hackathon non iniziato
            StaffProfile target = new StaffProfile("Nuovo", "Mentore", new Email("new@h.it"), null, StaffRole.MENTOR);

            when(mockStaffRepo.constructed().get(0).findById(mockSession, 5L)).thenReturn(Optional.of(dummyOrg));
            when(mockStaffRepo.constructed().get(0).findByEmail(mockSession, "new@h.it")).thenReturn(Optional.of(target));
            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));

            // Verifica che il mittente sia effettivamente l'organizzatore di quel hackathon
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

            // Simuliamo che non esista alcun organizzatore assegnato per questo evento
            when(mockAssignRepo.constructed().get(0).findByHackathonAndRole(mockSession, 100L, StaffRole.ORGANIZER))
                    .thenReturn(Optional.empty());

            IllegalAccessError ex = assertThrows(IllegalAccessError.class, () -> handler.sendRoleRequest(5L, "new@h.it", 100L, StaffRole.MENTOR));
            assertTrue(ex.getMessage().contains("Solo l'Organizzatore"));
        }

        @Test
        @DisplayName("Stato Macchina: Valutazione Approva -> Sostituisce ruolo e salva")
        void testEvaluateRoleRequestApprove() {
            RoleRequest req = new RoleRequest(dummyHackathon, dummyOrg, StaffRole.JUDGE);
            req.setId(77L); // Stato di default è PENDING

            when(mockRoleReqRepo.constructed().get(0).findById(mockSession, 77L)).thenReturn(Optional.of(req));
            when(mockAssignRepo.constructed().get(0).findByHackathonAndRole(mockSession, 100L, StaffRole.JUDGE))
                    .thenReturn(Optional.empty()); // Nessun giudice precedente

            handler.evaluateRoleRequest(77L, true);

            assertEquals(RequestState.APPROVED, req.getState());
            verify(mockAssignRepo.constructed().get(0), times(1)).save(eq(mockSession), any(StaffAssignment.class));
            verify(mockRoleReqRepo.constructed().get(0), times(1)).save(mockSession, req);
        }

        @Test
        @DisplayName("Stato Macchina: Valutazione Rifiuta -> Cambia stato ma NON assegna ruolo")
        void testEvaluateRoleRequestDeny() {
            RoleRequest req = new RoleRequest(dummyHackathon, dummyOrg, StaffRole.JUDGE);
            req.setId(77L);

            when(mockRoleReqRepo.constructed().get(0).findById(mockSession, 77L)).thenReturn(Optional.of(req));

            handler.evaluateRoleRequest(77L, false);

            assertEquals(RequestState.DENIED, req.getState());
            verify(mockAssignRepo.constructed().get(0), never()).save(any(), any()); // Nessuna assegnazione
        }

        @Test
        @DisplayName("Stato Macchina: Impossibile valutare una richiesta non PENDING")
        void testEvaluateAlreadyProcessed() {
            RoleRequest req = new RoleRequest(dummyHackathon, dummyOrg, StaffRole.JUDGE);
            req.setId(77L);
            req.setState(RequestState.DENIED); // Già processata

            when(mockRoleReqRepo.constructed().get(0).findById(mockSession, 77L)).thenReturn(Optional.of(req));

            assertThrows(IllegalStateException.class, () -> handler.evaluateRoleRequest(77L, true));
        }
    }

    @Nested
    @DisplayName("3. SubmissionHandler Tests (Orchestrazione Consegne)")
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
        @DisplayName("Invio: Creazione di una nuova sottomissione (Flusso completo)")
        void testSendSubmissionNew() {
            // L'hackathon deve essere in corso per poter inviare
            dummyHackathon.setExecutionStartDate(LocalDate.now());
            dummyHackathon.setExecutionEndDate(LocalDate.now());

            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));
            when(mockUserRepo.constructed().get(0).findById(mockSession, 1L)).thenReturn(Optional.of(dummyLeader));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 1L)).thenReturn(Optional.of(dummyTeam));
            when(mockPartTeamRepo.constructed().get(0).findByTeamAndHackathon(mockSession, 10L, 100L)).thenReturn(Optional.of(dummyPT));
            when(mockDelivRepo.constructed().get(0).findById(mockSession, 50L)).thenReturn(Optional.of(dummyDelivery));

            // Nessuna sottomissione precedente
            when(mockSubRepo.constructed().get(0).getByDeliveryAndTeam(mockSession, 50L, 88L)).thenReturn(Optional.empty());

            assertDoesNotThrow(() -> handler.sendSubmission(100L, 50L, 1L, "Il mio codice"));

            // Verifica che il salvataggio crei una nuova istanza
            verify(mockSubRepo.constructed().get(0), times(1)).save(eq(mockSession), any(Submission.class));
        }

        @Test
        @DisplayName("Invio: Aggiornamento di una sottomissione ESISTENTE (Upsert)")
        void testSendSubmissionUpdateExisting() {
            dummyHackathon.setExecutionStartDate(LocalDate.now());
            dummyHackathon.setExecutionEndDate(LocalDate.now());

            Submission existingSub = new Submission(dummyPT, dummyDelivery);

            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));
            when(mockUserRepo.constructed().get(0).findById(mockSession, 1L)).thenReturn(Optional.of(dummyLeader));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 1L)).thenReturn(Optional.of(dummyTeam));
            when(mockPartTeamRepo.constructed().get(0).findByTeamAndHackathon(mockSession, 10L, 100L)).thenReturn(Optional.of(dummyPT));
            when(mockDelivRepo.constructed().get(0).findById(mockSession, 50L)).thenReturn(Optional.of(dummyDelivery));

            // Simula esistenza precedente
            when(mockSubRepo.constructed().get(0).getByDeliveryAndTeam(mockSession, 50L, 88L)).thenReturn(Optional.of(existingSub));

            handler.sendSubmission(100L, 50L, 1L, "Codice Aggiornato");

            assertEquals("Codice Aggiornato", existingSub.getContent());
            verify(mockSubRepo.constructed().get(0), times(1)).save(mockSession, existingSub);
        }

        @Test
        @DisplayName("Sicurezza: Invio fallito se l'Hackathon non è in esecuzione")
        void testSendSubmissionNotInExecution() {
            dummyHackathon.setExecutionStartDate(LocalDate.now().plusDays(2)); // Inizia dopodomani
            when(mockHackRepo.constructed().get(0).findById(mockSession, 100L)).thenReturn(Optional.of(dummyHackathon));

            assertThrows(IllegalStateException.class, () -> handler.sendSubmission(100L, 50L, 1L, "Codice"));
        }

        @Test
        @DisplayName("Visualizzazione Giudice: Successo se assegnato all'evento")
        void testGetSubmissionsByStaffWithAccess() {
            StaffProfile judge = new StaffProfile("Mario", "Rossi", new Email("j@h.it"), null, StaffRole.JUDGE);
            StaffAssignment assignment = new StaffAssignment(judge, dummyHackathon, StaffRole.JUDGE);
            List<StaffAssignment> assignments = new ArrayList<>();
            assignments.add(assignment);

            when(mockStaffRepo.constructed().get(0).findById(mockSession, 3L)).thenReturn(Optional.of(judge));
            when(mockAssignRepo.constructed().get(0).findByStaffProfile(mockSession, 3L)).thenReturn(assignments);

            assertDoesNotThrow(() -> handler.getHackathonSubmissions(100L, 3L));
            verify(mockSubRepo.constructed().get(0), times(1)).getByHackathonIdOrderedByDelivery(mockSession, 100L);
        }

        @Test
        @DisplayName("Visualizzazione Giudice: Errore se lo staff NON è assegnato a questo Hackathon")
        void testGetSubmissionsByStaffNoAccess() {
            StaffProfile judge = new StaffProfile("Mario", "Rossi", new Email("j@h.it"), null, StaffRole.JUDGE);

            // Assegnato a un hackathon DIVERSO (ID 999)
            Hackathon otherHackathon = new Hackathon();
            otherHackathon.setId(999L);
            StaffAssignment assignment = new StaffAssignment(judge, otherHackathon, StaffRole.JUDGE);
            List<StaffAssignment> assignments = new ArrayList<>();
            assignments.add(assignment);

            when(mockStaffRepo.constructed().get(0).findById(mockSession, 3L)).thenReturn(Optional.of(judge));
            when(mockAssignRepo.constructed().get(0).findByStaffProfile(mockSession, 3L)).thenReturn(assignments);

            IllegalAccessError ex = assertThrows(IllegalAccessError.class, () -> handler.getHackathonSubmissions(100L, 3L));
            assertTrue(ex.getMessage().contains("non assegnato a questo hackathon"));
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
            when(mockTeamRepo.constructed().get(0).existsByName(mockSession, "Alpha")).thenReturn(true); // Nome in uso

            assertThrows(IllegalArgumentException.class, () -> handler.createTeam(1L, "Alpha"));
        }

        @Test
        @DisplayName("Sicurezza: L'invito fallisce se inviato da un NON leader")
        void testInviteNotLeader() {
            User target = new User("Target", new Email("t@h.it"), new UserPassword("Pwd12345!"));

            when(mockUserRepo.constructed().get(0).findByUsername(mockSession, "Target")).thenReturn(Optional.of(target));
            when(mockUserRepo.constructed().get(0).findById(mockSession, 2L)).thenReturn(Optional.of(dummyMember));
            when(mockTeamRepo.constructed().get(0).getTeamOfUser(mockSession, 2L)).thenReturn(Optional.of(dummyTeam));
            // dummyTeam è guidato da dummyLeader, non da dummyMember

            assertThrows(IllegalStateException.class, () -> handler.inviteUserToTeam(2L, "Target"));
        }
    }

    /**
     * Metodo di utilità (Helper) per forzare l'ID nelle entità senza usare i setter.
     * Perfetto per il Rich Domain dove gli ID sono immutabili per il codice business.
     */
    private void setEntityId(Object entity, Long id) {
        try {
            java.lang.reflect.Field idField = null;
            Class<?> currentClass = entity.getClass();

            // Risale la gerarchia delle classi finché non trova il campo 'id'
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

            idField.setAccessible(true); // Scavalca l'incapsulamento privato
            idField.set(entity, id);     // Inietta il valore

        } catch (Exception e) {
            throw new RuntimeException("Errore durante il setting dell'ID via Reflection", e);
        }
    }
}