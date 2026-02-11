package it.hackhub.model;

import it.hackhub.model.enums.ChatParticipantType;
import it.hackhub.model.enums.RequestState;
import it.hackhub.model.enums.StaffRole;
import it.hackhub.model.valueobjs.Email;
import it.hackhub.model.valueobjs.GitHubUrl;
import it.hackhub.model.valueobjs.UserPassword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Suite Completa: Rich Domain (47 Test)")
class RichDomainTest {

    private User dummyLeader;
    private User dummyMember;
    private User dummyNewUser;
    private StaffProfile dummyJudge;
    private StaffProfile dummyMentor;
    private Admin dummyAdmin;
    private Team dummyTeam;
    private Hackathon dummyHackathon;

    @BeforeEach
    void setUp() {
        UserPassword pwd = new UserPassword("SecurePass123!");
        dummyLeader = new User("LeaderUser", new Email("leader@hackhub.it"), pwd);
        dummyLeader.setId(1L);

        dummyMember = new User("MemberUser", new Email("member@hackhub.it"), pwd);
        dummyMember.setId(2L);

        dummyNewUser = new User("NewUser", new Email("new@hackhub.it"), pwd);
        dummyNewUser.setId(99L);

        dummyJudge = new StaffProfile("Mario", "Rossi", new Email("judge@hackhub.it"), pwd, StaffRole.JUDGE);
        dummyJudge.setId(3L);

        dummyMentor = new StaffProfile("Luigi", "Verdi", new Email("mentor@hackhub.it"), pwd, StaffRole.MENTOR);
        dummyMentor.setId(4L);

        dummyAdmin = new Admin(new Email("admin@hackhub.it"), pwd);
        dummyAdmin.setId(999L);

        dummyTeam = new Team("Alpha", dummyLeader);
        dummyHackathon = new Hackathon();
    }

    @Nested
    @DisplayName("1. Test Invarianti su Team e Leadership (6 Test)")
    class TeamTests {
        @Test
        @DisplayName("La creazione del team imposta il creatore come leader")
        void testTeamCreationSetsLeader() {
            assertTrue(dummyTeam.isLeader(dummyLeader));
        }

        @Test
        @DisplayName("La creazione del team aggiunge il leader ai membri (Size = 1)")
        void testTeamCreationAddsLeaderToMembers() {
            assertEquals(1, dummyTeam.getSize());
            assertTrue(dummyTeam.getMembers().contains(dummyLeader));
        }

        @Test
        @DisplayName("Aggiungere un membro normale aumenta la size ma non cambia la leadership")
        void testAddMemberIncreasesSize() {
            dummyTeam.addMember(dummyMember);
            assertEquals(2, dummyTeam.getSize());
            assertFalse(dummyTeam.isLeader(dummyMember));
        }

        @Test
        @DisplayName("Aggiungere un membro duplicato viene ignorato (Set Invariant)")
        void testAddDuplicateMember() {
            dummyTeam.addMember(dummyMember);
            dummyTeam.addMember(dummyMember);
            assertEquals(2, dummyTeam.getSize());
        }

        @Test
        @DisplayName("Assegnare un nuovo leader esterno lo aggiunge automaticamente ai membri")
        void testSetLeaderToNewUser() {
            dummyTeam.setLeader(dummyNewUser);
            assertTrue(dummyTeam.isLeader(dummyNewUser));
            assertEquals(2, dummyTeam.getSize());
            assertTrue(dummyTeam.getMembers().contains(dummyNewUser));
            assertTrue(dummyTeam.getMembers().contains(dummyLeader), "Il vecchio leader resta membro");
        }

        @Test
        @DisplayName("Assegnare un nuovo leader già membro non duplica i dati")
        void testSetLeaderToExistingMember() {
            dummyTeam.addMember(dummyMember);
            dummyTeam.setLeader(dummyMember);
            assertTrue(dummyTeam.isLeader(dummyMember));
            assertEquals(2, dummyTeam.getSize());
        }
    }

    @Nested
    @DisplayName("2. Test Macchina a Stati: Hackathon Execution (8 Test)")
    class HackathonExecutionTests {
        @Test
        @DisplayName("isInExecution = TRUE se oggi è tra inizio e fine")
        void testExecutionInsideRange() {
            dummyHackathon.setExecutionStartDate(LocalDate.now().minusDays(2));
            dummyHackathon.setExecutionEndDate(LocalDate.now().plusDays(2));
            assertTrue(dummyHackathon.isInExecution());
        }

        @Test
        @DisplayName("isInExecution = TRUE esattamente nel giorno di inizio (Boundary)")
        void testExecutionExactlyOnStart() {
            dummyHackathon.setExecutionStartDate(LocalDate.now());
            dummyHackathon.setExecutionEndDate(LocalDate.now().plusDays(5));
            assertTrue(dummyHackathon.isInExecution());
        }

        @Test
        @DisplayName("isInExecution = TRUE esattamente nel giorno di fine (Boundary)")
        void testExecutionExactlyOnEnd() {
            dummyHackathon.setExecutionStartDate(LocalDate.now().minusDays(5));
            dummyHackathon.setExecutionEndDate(LocalDate.now());
            assertTrue(dummyHackathon.isInExecution());
        }

        @Test
        @DisplayName("isInExecution = FALSE se l'evento inizia domani")
        void testExecutionBeforeStart() {
            dummyHackathon.setExecutionStartDate(LocalDate.now().plusDays(1));
            dummyHackathon.setExecutionEndDate(LocalDate.now().plusDays(5));
            assertFalse(dummyHackathon.isInExecution());
        }

        @Test
        @DisplayName("isInExecution = FALSE se l'evento è finito ieri")
        void testExecutionAfterEnd() {
            dummyHackathon.setExecutionStartDate(LocalDate.now().minusDays(5));
            dummyHackathon.setExecutionEndDate(LocalDate.now().minusDays(1));
            assertFalse(dummyHackathon.isInExecution());
        }

        @Test
        @DisplayName("isInExecution = FALSE (Null Safety) se start date è null")
        void testExecutionNullStart() {
            dummyHackathon.setExecutionEndDate(LocalDate.now());
            assertFalse(dummyHackathon.isInExecution());
        }

        @Test
        @DisplayName("isInExecution = FALSE (Null Safety) se end date è null")
        void testExecutionNullEnd() {
            dummyHackathon.setExecutionStartDate(LocalDate.now());
            assertFalse(dummyHackathon.isInExecution());
        }

        @Test
        @DisplayName("isInExecution = FALSE (Null Safety) se entrambe le date sono null")
        void testExecutionBothNull() {
            assertFalse(dummyHackathon.isInExecution());
        }
    }

    @Nested
    @DisplayName("3. Test Invarianti: Hackathon Subscriptions e Roles (9 Test)")
    class HackathonValidationTests {
        @Test
        @DisplayName("Iscrizioni aperte se oggi è nel range")
        void testSubscriptionInsideRange() {
            dummyHackathon.setSubscriptionStartDate(LocalDate.now().minusDays(1));
            dummyHackathon.setSubscriptionEndDate(LocalDate.now().plusDays(1));
            assertDoesNotThrow(dummyHackathon::assertIsOpenForSubscription);
        }

        @Test
        @DisplayName("Iscrizioni aperte esattamente nei giorni di inizio e fine (Boundaries)")
        void testSubscriptionBoundaries() {
            dummyHackathon.setSubscriptionStartDate(LocalDate.now());
            dummyHackathon.setSubscriptionEndDate(LocalDate.now());
            assertDoesNotThrow(dummyHackathon::assertIsOpenForSubscription);
        }

        @Test
        @DisplayName("Iscrizioni rifiutate se aprono domani")
        void testSubscriptionThrowsBeforeStart() {
            dummyHackathon.setSubscriptionStartDate(LocalDate.now().plusDays(1));
            dummyHackathon.setSubscriptionEndDate(LocalDate.now().plusDays(5));
            assertThrows(IllegalStateException.class, dummyHackathon::assertIsOpenForSubscription);
        }

        @Test
        @DisplayName("Iscrizioni rifiutate se chiuse ieri")
        void testSubscriptionThrowsAfterEnd() {
            dummyHackathon.setSubscriptionStartDate(LocalDate.now().minusDays(5));
            dummyHackathon.setSubscriptionEndDate(LocalDate.now().minusDays(1));
            assertThrows(IllegalStateException.class, dummyHackathon::assertIsOpenForSubscription);
        }

        @Test
        @DisplayName("Iscrizioni rifiutate se le date non sono impostate (Null Safety)")
        void testSubscriptionThrowsIfNulls() {
            assertThrows(IllegalStateException.class, dummyHackathon::assertIsOpenForSubscription);
        }

        @Test
        @DisplayName("Nomine valide prima che l'evento inizi")
        void testRolesValidBeforeExecution() {
            dummyHackathon.setExecutionStartDate(LocalDate.now().plusDays(5));
            assertDoesNotThrow(dummyHackathon::assertApprovedOrSubscription);
        }

        @Test
        @DisplayName("Nomine valide ESATTAMENTE nel giorno in cui l'evento inizia")
        void testRolesValidOnExecutionDay() {
            dummyHackathon.setExecutionStartDate(LocalDate.now());
            assertDoesNotThrow(dummyHackathon::assertApprovedOrSubscription);
        }

        @Test
        @DisplayName("Nomine rifiutate se l'evento è già in esecuzione da ieri")
        void testRolesThrowsAfterExecutionStarted() {
            dummyHackathon.setExecutionStartDate(LocalDate.now().minusDays(1));
            assertThrows(IllegalStateException.class, dummyHackathon::assertApprovedOrSubscription);
        }

        @Test
        @DisplayName("Nomine valide se le date non sono ancora state decise")
        void testRolesValidIfNoDatesSet() {
            assertDoesNotThrow(dummyHackathon::assertApprovedOrSubscription);
        }
    }

    @Nested
    @DisplayName("4. Test Polimorfismo e Relazioni Bidirezionali: Deliveries (2 Test)")
    class DeliveriesTests {
        @Test
        @DisplayName("Aggiunta StandardDelivery imposta il riferimento genitore")
        void testAddStandardDelivery() {
            StandardDelivery std = new StandardDelivery("Task 1");
            dummyHackathon.addDelivery(std);
            assertEquals(1, dummyHackathon.getDeliveries().size());
            assertEquals(dummyHackathon, std.getHackathon());
        }

        @Test
        @DisplayName("Aggiunta FlagDelivery imposta il riferimento genitore e salva la solution")
        void testAddFlagDelivery() {
            FlagDelivery flag = new FlagDelivery("Find flag", "CTF{123}");
            dummyHackathon.addDelivery(flag);
            assertEquals(1, dummyHackathon.getDeliveries().size());
            assertEquals(dummyHackathon, flag.getHackathon());
            assertEquals("CTF{123}", flag.getSolution());
        }
    }

    @Nested
    @DisplayName("5. Test Invarianti: Submission con GitHubUrl (9 Test)")
    class SubmissionTests {
        private Submission submission;

        @BeforeEach
        void initSub() {
            submission = new Submission(new ParticipatingTeam(dummyTeam, dummyHackathon), new StandardDelivery("T"));
        }

        @Test
        @DisplayName("L'update sovrascrive l'URL GitHub")
        void testUpdateChangesGitHubUrl() {
            GitHubUrl url1 = new GitHubUrl("https://github.com/team/repo1");
            submission.update(url1);
            assertEquals(url1, submission.getRepositoryUrl());

            GitHubUrl url2 = new GitHubUrl("https://github.com/team/repo2");
            submission.update(url2);
            assertEquals(url2, submission.getRepositoryUrl());
        }

        @Test
        @DisplayName("L'update viene bloccato se la sottomissione è già stata valutata")
        void testUpdateFailsIfAlreadyEvaluated() {
            submission.evaluate(80, "Ottimo lavoro", dummyJudge);

            GitHubUrl newUrl = new GitHubUrl("https://github.com/team/repo-nuovo");
            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> submission.update(newUrl));
            assertTrue(ex.getMessage().toLowerCase().contains("già valutata"));
        }

        @Test
        @DisplayName("Evaluate accetta il confine minimo valido (0)")
        void testEvaluateAcceptsZero() {
            assertDoesNotThrow(() -> submission.evaluate(0, "Male", dummyJudge));
            assertEquals(0, submission.getScore());
        }

        @Test
        @DisplayName("Evaluate accetta il confine massimo valido (100)")
        void testEvaluateAcceptsOneHundred() {
            assertDoesNotThrow(() -> submission.evaluate(100, "Perfetto", dummyJudge));
            assertEquals(100, submission.getScore());
        }

        @Test
        @DisplayName("Evaluate accetta valori intermedi (50)")
        void testEvaluateAcceptsMidValue() {
            assertDoesNotThrow(() -> submission.evaluate(50, "Medio", dummyJudge));
            assertEquals(50, submission.getScore());
        }

        @Test
        @DisplayName("Evaluate rifiuta valori negativi (-1)")
        void testEvaluateRejectsNegative() {
            assertThrows(IllegalArgumentException.class, () -> submission.evaluate(-1, "Err", dummyJudge));
        }

        @Test
        @DisplayName("Evaluate rifiuta valori oltre il limite (101)")
        void testEvaluateRejectsOverLimit() {
            assertThrows(IllegalArgumentException.class, () -> submission.evaluate(101, "Err", dummyJudge));
        }

        @Test
        @DisplayName("Evaluate svela l'autoboxing lanciando NPE se il voto è null")
        void testEvaluateNullThrowsNPE() {
            assertThrows(NullPointerException.class, () -> submission.evaluate(null, "Err", dummyJudge));
        }

        @Test
        @DisplayName("Evaluate sovrascrive un voto e un giudice precedenti")
        void testEvaluateOverrides() {
            submission.evaluate(50, "Bozza", dummyJudge);
            submission.evaluate(90, "Corretto", dummyMentor); // Cambio giudice
            assertEquals(90, submission.getScore());
            assertEquals("Corretto", submission.getWrittenEvaluation());
            assertEquals(dummyMentor, submission.getJudge());
        }
    }

    @Nested
    @DisplayName("6. Test Ereditarietà: Account e Security (4 Test)")
    class AccountTests {
        @Test
        @DisplayName("Controllo password corretto su User")
        void testUserPasswordSuccess() {
            assertTrue(dummyLeader.checkPassword("SecurePass123!"));
        }

        @Test
        @DisplayName("Controllo password fallito su User")
        void testUserPasswordFail() {
            assertFalse(dummyLeader.checkPassword("WrongPass!"));
        }

        @Test
        @DisplayName("Controllo password polimorfico funziona anche su StaffProfile")
        void testStaffPasswordSuccess() {
            assertTrue(dummyJudge.checkPassword("SecurePass123!"));
        }

        @Test
        @DisplayName("Controllo password polimorfico funziona anche su Admin")
        void testAdminPasswordSuccess() {
            assertTrue(dummyAdmin.checkPassword("SecurePass123!"));
        }
    }

    @Nested
    @DisplayName("7. Test Relazioni: Support Chat e Call Request (3 Test)")
    class SupportChatTests {
        @Test
        @DisplayName("Aggiunta Messaggio sincronizza SupportChat bidirezionalmente")
        void testAddMessageSyncs() {
            SupportChat chat = new SupportChat(new ParticipatingTeam(dummyTeam, dummyHackathon), dummyMentor);
            Message msg = new Message("Help", ChatParticipantType.TEAM_MEMBER, dummyLeader.getId());
            chat.addMessage(msg);

            assertEquals(1, chat.getMessages().size());
            assertEquals(chat, msg.getSupportChat());
        }

        @Test
        @DisplayName("La creazione del Message auto-assegna il Timestamp")
        void testMessageCreationSetsTimestamp() {
            Message msg = new Message("Help", ChatParticipantType.TEAM_MEMBER, dummyLeader.getId());
            assertNotNull(msg.getTimestamp());
        }

        @Test
        @DisplayName("CallRequest imposta dati correttamente")
        void testCallRequestSetup() {
            SupportChat chat = new SupportChat(new ParticipatingTeam(), dummyMentor);
            LocalDateTime proposed = LocalDateTime.now().plusDays(2);
            CallRequest req = new CallRequest(chat, proposed, "Topic");

            assertEquals(chat, req.getSupportChat());
            assertEquals(proposed, req.getProposedTime());
            assertEquals("Topic", req.getTopic());
        }
    }

    @Nested
    @DisplayName("8. Test Ereditarietà e Macchina a Stati: Requests (6 Test)")
    class RequestStateTests {
        @Test
        @DisplayName("JoinTeamRequest nasce in stato PENDING")
        void testJoinTeamRequestPending() {
            JoinTeamRequest req = new JoinTeamRequest(dummyTeam, dummyNewUser);
            assertEquals(RequestState.PENDING, req.getState());
        }

        @Test
        @DisplayName("RoleRequest nasce in stato PENDING")
        void testRoleRequestPending() {
            RoleRequest req = new RoleRequest(dummyHackathon, dummyJudge, StaffRole.JUDGE);
            assertEquals(RequestState.PENDING, req.getState());
        }

        @Test
        @DisplayName("HackathonRequest nasce in stato PENDING (Test via Reflection robusta)")
        void testHackathonRequestPending() throws Exception {
            HackathonRequest req = new HackathonRequest(dummyHackathon, dummyJudge);

            java.lang.reflect.Field field = null;
            Class<?> currentClass = req.getClass();

            while (currentClass != null && field == null) {
                try {
                    field = currentClass.getDeclaredField("status");
                } catch (NoSuchFieldException e1) {
                    try {
                        field = currentClass.getDeclaredField("state");
                    } catch (NoSuchFieldException e2) {
                        currentClass = currentClass.getSuperclass();
                    }
                }
            }

            if (field == null) {
                fail("Impossibile trovare il campo 'status' o 'state' in HackathonRequest o nelle sue superclassi.");
            }

            field.setAccessible(true);

            Object actualStatus = field.get(req);

            assertEquals("PENDING", actualStatus.toString(), "HackathonRequest deve nascere in stato PENDING");
        }

        @Test
        @DisplayName("CallRequest nasce in stato PENDING (Ereditato)")
        void testCallRequestPending() {
            CallRequest req = new CallRequest(new SupportChat(), LocalDateTime.now(), "");
            assertEquals(RequestState.PENDING, req.getState());
        }

        @Test
        @DisplayName("Transizione di stato PENDING -> APPROVED")
        void testStateTransitionToApproved() {
            RoleRequest req = new RoleRequest(dummyHackathon, dummyJudge, StaffRole.JUDGE);
            req.setState(RequestState.APPROVED);
            assertEquals(RequestState.APPROVED, req.getState());
        }

        @Test
        @DisplayName("Transizione di stato PENDING -> DENIED")
        void testStateTransitionToDenied() {
            RoleRequest req = new RoleRequest(dummyHackathon, dummyJudge, StaffRole.JUDGE);
            req.setState(RequestState.DENIED);
            assertEquals(RequestState.DENIED, req.getState());
        }
    }
}