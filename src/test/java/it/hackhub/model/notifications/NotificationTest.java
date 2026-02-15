package it.hackhub.model.notifications;

import it.hackhub.model.*;
import it.hackhub.model.accounts.StaffProfile;
import it.hackhub.model.accounts.User;
import it.hackhub.model.enums.StaffRole;
import it.hackhub.model.valueobjs.Email;
import it.hackhub.model.valueobjs.UserPassword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Suite: Notification System & Security")
class NotificationTest {

    private User userAlice;
    private User userBob;
    private StaffProfile staffJudge;

    private List<Notification> mockDatabase;

    @BeforeEach
    void setUp() {
        UserPassword pwd = new UserPassword("Pass123!");

        userAlice = new User("Alice", new Email("alice@test.com"), pwd);
        userAlice.setId(10L);

        userBob = new User("Bob", new Email("bob@test.com"), pwd);
        userBob.setId(20L);

        staffJudge = new StaffProfile("Judge", "Dredd", new Email("dredd@test.com"), pwd, StaffRole.JUDGE);
        staffJudge.setId(30L);

        mockDatabase = new ArrayList<>();
    }

    @Nested
    @DisplayName("1. Factory Pattern Tests")
    class FactoryTests {

        @Test
        @DisplayName("SystemNotificationFactory crea SystemNotification corretta")
        void testSystemFactory() {
            NotificationFactory factory = new SystemNotificationFactory();
            Notification n = factory.createNotification(userAlice, "Benvenuto!");

            assertTrue(n instanceof SystemNotification);
            assertEquals("Benvenuto!", n.getContent());
            assertEquals(userAlice, n.getRecipient());
            assertFalse(n.isRead());
            assertNotNull(n.getTimestamp());
        }

        @Test
        @DisplayName("InviteFactory (Team Mode) imposta TargetTeamId")
        void testInviteFactoryTeamMode() {
            NotificationFactory factory = new InviteNotificationFactory(100L);
            Notification n = factory.createNotification(userBob, "Join my team");

            assertTrue(n instanceof InviteNotification);
            InviteNotification invite = (InviteNotification) n;

            assertEquals(100L, invite.getTargetTeamId());
            assertNull(invite.getTargetHackathonId());
            assertEquals(userBob, n.getRecipient());
        }

        @Test
        @DisplayName("InviteFactory (Hackathon Mode) imposta HackathonId e Ruolo")
        void testInviteFactoryHackathonMode() {
            NotificationFactory factory = new InviteNotificationFactory(500L, "MENTOR");
            Notification n = factory.createNotification(staffJudge, "Please be a mentor");

            assertTrue(n instanceof InviteNotification);
            InviteNotification invite = (InviteNotification) n;

            assertEquals(500L, invite.getTargetHackathonId());
            assertEquals("MENTOR", invite.getRoleProposed());
            assertNull(invite.getTargetTeamId());
        }
    }

    @Nested
    @DisplayName("2. Account Integration & Polymorphism")
    class IntegrationTests {

        @Test
        @DisplayName("User.notify() aggiunge la notifica alla lista locale dell'utente")
        void testUserNotify() {
            NotificationFactory factory = new SystemNotificationFactory();
            userAlice.notify(factory, "Messaggio per Alice");

            assertEquals(1, userAlice.getNotifications().size());
            assertEquals("Messaggio per Alice", userAlice.getNotifications().get(0).getContent());
            assertEquals(userAlice, userAlice.getNotifications().get(0).getRecipient());
        }

        @Test
        @DisplayName("StaffProfile.notify() funziona identicamente a User (Polimorfismo)")
        void testStaffNotify() {
            NotificationFactory factory = new SystemNotificationFactory();
            staffJudge.notify(factory, "Messaggio per Giudice");

            assertEquals(1, staffJudge.getNotifications().size());
            assertEquals("Messaggio per Giudice", staffJudge.getNotifications().get(0).getContent());
            assertEquals(staffJudge, staffJudge.getNotifications().get(0).getRecipient());
        }
    }

    @Nested
    @DisplayName("3. Security & Isolation Tests (Cruciale)")
    class IsolationTests {

        @BeforeEach
        void populateMockDB() {
            NotificationFactory sysFactory = new SystemNotificationFactory();

            mockDatabase.add(userAlice.notify(sysFactory, "Alice Notif 1"));
            mockDatabase.add(userAlice.notify(sysFactory, "Alice Notif 2"));

            mockDatabase.add(userBob.notify(sysFactory, "Bob Notif 1"));

            mockDatabase.add(staffJudge.notify(sysFactory, "Judge Notif 1"));
        }

        @Test
        @DisplayName("Alice deve vedere SOLO le notifiche di Alice")
        void testAliceIsolation() {
            List<Notification> aliceResults = mockDatabase.stream()
                    .filter(n -> n.getRecipient().getId().equals(userAlice.getId()))
                    .collect(Collectors.toList());

            assertEquals(2, aliceResults.size());

            boolean hasIntruders = aliceResults.stream()
                    .anyMatch(n -> !n.getRecipient().equals(userAlice));

            assertFalse(hasIntruders, "Trovate notifiche di altri utenti nel feed di Alice!");
        }

        @Test
        @DisplayName("Lo Staff deve vedere SOLO le notifiche dello Staff")
        void testStaffIsolation() {
            List<Notification> staffResults = mockDatabase.stream()
                    .filter(n -> n.getRecipient().getId().equals(staffJudge.getId()))
                    .collect(Collectors.toList());

            assertEquals(1, staffResults.size());
            assertEquals("Judge Notif 1", staffResults.get(0).getContent());
            assertEquals(staffJudge, staffResults.get(0).getRecipient());
        }

        @Test
        @DisplayName("Bob non deve vedere le notifiche di Alice")
        void testCrossUserAccessDenied() {
            List<Notification> bobResults = mockDatabase.stream()
                    .filter(n -> n.getRecipient().getId().equals(userBob.getId()))
                    .collect(Collectors.toList());

            boolean containsAliceData = bobResults.stream()
                    .anyMatch(n -> n.getRecipient().getId().equals(userAlice.getId()));

            assertFalse(containsAliceData, "Violazione di sicurezza: Bob vede i dati di Alice");
        }
    }
}
