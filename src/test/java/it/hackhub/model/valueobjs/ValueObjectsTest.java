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
            // Controlla solo che venga lanciata l'eccezione, ignorando il testo esatto
            assertThrows(IllegalArgumentException.class, () -> new Email(invalidEmail));
        }

        @ParameterizedTest(name = "Formato errato: ''{0}''")
        @ValueSource(strings = {
                "plainaddress",
                "@missinguser.com",
                "user@",
                "user space@dom.com"
        })
        @DisplayName("Deve fallire con formati non conformi alla Regex")
        void testEmailFormatInvalid(String badFormat) {
            assertThrows(IllegalArgumentException.class, () -> new Email(badFormat));
        }

        @ParameterizedTest(name = "Email valida: ''{0}''")
        @ValueSource(strings = {
                "test@hackhub.it",
                "user.name+tag@domain.co.uk",
                "12345@numbers.com",
                "a@b.c"
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
            assertThrows(IllegalArgumentException.class, () -> new UserPassword(invalidPwd));
        }

        @ParameterizedTest(name = "Password troppo corta: ''{0}''")
        @ValueSource(strings = {"1", "123456", "1234567"})
        @DisplayName("Deve fallire con password lunghe meno di 8 caratteri (Boundary Test)")
        void testPasswordTooShort(String shortPwd) {
            assertThrows(IllegalArgumentException.class, () -> new UserPassword(shortPwd));
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

            assertTrue(pwd.match("MySecurePassword123!"));
            assertFalse(pwd.match("WrongPassword!"));

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
            LocalDateTime end = start.minusDays(1);

            assertThrows(IllegalArgumentException.class, () -> new Period(start, end));
        }

        @Test
        @DisplayName("Deve istanziare se le date coincidono (Boundary Test) o se la fine è successiva")
        void testPeriodValidDates() {
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime endSame = start;
            LocalDateTime endFuture = start.plusHours(2);

            assertDoesNotThrow(() -> new Period(start, endSame));
            assertDoesNotThrow(() -> new Period(start, endFuture));
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
            assertThrows(IllegalArgumentException.class,
                    () -> new Info(badName, "Tipo", "Subtipo", 100.0, true));
        }

        @Test
        @DisplayName("Deve fallire con premio negativo")
        void testInfoNegativePrize() {
            assertThrows(IllegalArgumentException.class,
                    () -> new Info("Valid Name", "Tipo", "Subtipo", -0.01, true));
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
    @DisplayName("Test su Rules (Aggiornato per DB Text)")
    class RulesTests {

        @ParameterizedTest(name = "Membri massimi: {0}")
        @ValueSource(ints = {0, -1, -100})
        @DisplayName("Deve fallire se il numero massimo di membri è zero o negativo (Boundary Test)")
        void testRulesInvalidMembers(int badMembers) {
            // Rimosso il check sul testo
            assertThrows(IllegalArgumentException.class,
                    () -> new Rules(badMembers, "Regola 1: Divertirsi."));
        }

        @ParameterizedTest(name = "Testo Regole invalido: ''{0}''")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Deve fallire se il testo delle regole è nullo o vuoto")
        void testRulesInvalidText(String badText) {
            assertThrows(IllegalArgumentException.class,
                    () -> new Rules(5, badText));
        }

        @Test
        @DisplayName("Deve istanziare correttamente con dati validi")
        void testRulesValid() {
            String fullText = "Art. 1: Il codice deve essere open source.\nArt. 2: I team non possono superare 4 persone.";
            Rules rules = new Rules(4, fullText);
            assertEquals(4, rules.getMaxTeamMembers());
            assertEquals(fullText, rules.getRulesText());
        }
    }

    @Nested
    @DisplayName("Test su GitHubUrl (Nuovo Value Object)")
    class GitHubUrlTests {

        @ParameterizedTest(name = "URL banale/invalido: ''{0}''")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("Deve fallire con valori nulli o vuoti")
        void testUrlTrivialInvalid(String invalidUrl) {
            assertThrows(IllegalArgumentException.class, () -> new GitHubUrl(invalidUrl));
        }

        @ParameterizedTest(name = "URL formato errato: ''{0}''")
        @ValueSource(strings = {
                "https://google.com",
                "github.com",
                "github.com/user_only",
                "https://gitlab.com/user/repo",
                "ftp://github.com/user/repo"
        })
        @DisplayName("Deve fallire con URL che non puntano a un repository GitHub valido")
        void testUrlFormatInvalid(String badFormat) {
            assertThrows(IllegalArgumentException.class, () -> new GitHubUrl(badFormat));
        }

        @ParameterizedTest(name = "URL valido: ''{0}''")
        @ValueSource(strings = {
                "https://github.com/mario-rossi/hackathon-project",
                "http://github.com/team_alpha/repo123",
                "www.github.com/user/repo",
                "github.com/user/repo.git",
                "https://github.com/user/repo/tree/main"
        })
        @DisplayName("Deve istanziare correttamente con URL GitHub validi")
        void testUrlValid(String validUrl) {
            GitHubUrl url = new GitHubUrl(validUrl);
            assertEquals(validUrl, url.getValue());
        }
    }
}