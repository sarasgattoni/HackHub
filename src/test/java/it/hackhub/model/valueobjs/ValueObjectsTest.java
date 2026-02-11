package it.hackhub.model.valueobjs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test Suite: Value Objects (Rich Domain)")
class ValueObjectsTest {

    @Nested
    @DisplayName("Test su Email")
    class EmailTests {

        @ParameterizedTest(name = "Valore banale/invalido: ''{0}''")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Deve fallire con valori nulli, vuoti o composti da soli spazi")
        void testEmailTrivialInvalid(String invalidEmail) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
            assertTrue(ex.getMessage().contains("cannot be null or empty"));
        }

        @ParameterizedTest(name = "Formato errato: ''{0}''")
        @ValueSource(strings = {
                "plainaddress",       // Manca la chiocciola
                "@missinguser.com",   // Manca l'utente
                "user@",              // Manca il dominio
                "user space@dom.com"  // Spazi non ammessi (in base alla tua regex)
        })
        @DisplayName("Deve fallire con formati non conformi alla Regex")
        void testEmailFormatInvalid(String badFormat) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Email(badFormat));
            assertTrue(ex.getMessage().contains("Invalid email address"));
        }

        @ParameterizedTest(name = "Email valida: ''{0}''")
        @ValueSource(strings = {
                "test@hackhub.it",
                "user.name+tag@domain.co.uk",
                "12345@numbers.com",
                "a@b.c" // Caso limite accettato dalla tua regex ^[A-Za-z0-9+_.-]+@(.+)$
        })
        @DisplayName("Deve istanziare correttamente con formati validi")
        void testEmailValid(String validEmail) {
            Email email = new Email(validEmail);
            assertEquals(validEmail, email.getValue());
        }
    }

    @Nested
    @DisplayName("Test su UserPassword")
    class UserPasswordTests {

        @ParameterizedTest(name = "Password banale: ''{0}''")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "      "})
        @DisplayName("Deve fallire con valori nulli o composti da soli spazi")
        void testPasswordTrivialInvalid(String invalidPwd) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new UserPassword(invalidPwd));
            assertTrue(ex.getMessage().contains("cannot be null or empty"));
        }

        @ParameterizedTest(name = "Password troppo corta: ''{0}''")
        @ValueSource(strings = {"1", "123456", "1234567"})
        @DisplayName("Deve fallire con password lunghe meno di 8 caratteri (Boundary Test)")
        void testPasswordTooShort(String shortPwd) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new UserPassword(shortPwd));
            assertTrue(ex.getMessage().contains("cannot be less than 8 characters"));
        }

        @Test
        @DisplayName("Deve istanziare con 8 caratteri esatti (Boundary Test)")
        void testPasswordExactlyEightChars() {
            assertDoesNotThrow(() -> new UserPassword("12345678"));
        }

        @Test
        @DisplayName("Deve far corrispondere la password in chiaro e offuscare il toString")
        void testPasswordMatchAndToString() {
            UserPassword pwd = new UserPassword("MySecurePassword123!");

            assertTrue(pwd.match("MySecurePassword123!"), "Il match deve avere successo con la stringa esatta");
            assertFalse(pwd.match("WrongPassword!"), "Il match deve fallire con stringa errata");

            assertEquals("********", pwd.toString(), "Il toString non deve mai rivelare la password in chiaro");
        }
    }

    @Nested
    @DisplayName("Test su Period")
    class PeriodTests {

        @Test
        @DisplayName("Deve fallire con date nulle")
        void testPeriodNullDates() {
            LocalDateTime now = LocalDateTime.now();
            assertThrows(IllegalArgumentException.class, () -> new Period(null, now));
            assertThrows(IllegalArgumentException.class, () -> new Period(now, null));
            assertThrows(IllegalArgumentException.class, () -> new Period(null, null));
        }

        @Test
        @DisplayName("Deve fallire se la data di fine precede la data di inizio")
        void testPeriodEndBeforeStart() {
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.minusDays(1); // Ieri

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new Period(start, end));
            assertTrue(ex.getMessage().contains("End date cannot be before start date"));
        }

        @Test
        @DisplayName("Deve istanziare se le date coincidono (Boundary Test) o se la fine è successiva")
        void testPeriodValidDates() {
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime endSame = start;
            LocalDateTime endFuture = start.plusHours(2);

            assertDoesNotThrow(() -> new Period(start, endSame), "Start uguale a End deve essere valido");
            assertDoesNotThrow(() -> new Period(start, endFuture), "Start prima di End deve essere valido");
        }
    }

    @Nested
    @DisplayName("Test su Info")
    class InfoTests {

        @ParameterizedTest(name = "Nome invalido: ''{0}''")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Deve fallire con nome nullo o vuoto")
        void testInfoInvalidName(String badName) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Info(badName, "Tipo", "Subtipo", 100.0, true));
            assertTrue(ex.getMessage().contains("obligatory"));
        }

        @Test
        @DisplayName("Deve fallire con premio negativo")
        void testInfoNegativePrize() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Info("Valid Name", "Tipo", "Subtipo", -0.01, true));
            assertTrue(ex.getMessage().contains("can't be negative"));
        }

        @Test
        @DisplayName("Deve applicare il fallback su stringa vuota se il subtype è nullo")
        void testInfoSubtypeNullFallback() {
            Info info = new Info("Valid Name", "Tipo", null, 100.0, false);
            assertEquals("", info.getSubtype(), "Il costruttore deve convertire il null in stringa vuota");
        }

        @Test
        @DisplayName("Deve istanziare con premio a 0 (Boundary Test)")
        void testInfoZeroPrize() {
            Info info = new Info("Valid Name", "Tipo", "Subtipo", 0.0, true);
            assertEquals(0.0, info.getPrize());
        }
    }

    @Nested
    @DisplayName("Test su Rules")
    class RulesTests {

        @ParameterizedTest(name = "Membri massimi: {0}")
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("Deve fallire se il numero massimo di membri è zero o negativo (Boundary Test)")
        void testRulesInvalidMembers(int badMembers) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Rules(badMembers, "/docs/rules.pdf"));
            assertTrue(ex.getMessage().contains("positive"));
        }

        @ParameterizedTest(name = "Documento invalido: ''{0}''")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Deve fallire se il percorso del documento è nullo o vuoto")
        void testRulesInvalidDocument(String badDoc) {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> new Rules(5, badDoc));
            assertTrue(ex.getMessage().contains("cannot be null or empty"));
        }

        @Test
        @DisplayName("Deve istanziare correttamente con dati validi")
        void testRulesValid() {
            Rules rules = new Rules(4, "https://storage.hackhub.it/rules_v1.pdf");
            assertEquals(4, rules.getMaxTeamMembers());
            assertEquals("https://storage.hackhub.it/rules_v1.pdf", rules.getRulesDocument());
        }
    }
}