package it.hackhub.model.valueobjs;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ValueObjectsTest {

    @Nested
    class InfoTests {

        @Test
        void shouldCreateValidInfo() {
            Info info = new Info("Hackathon 2025", "AI Challenge", "Generative AI", 5000.0, false);

            assertNotNull(info);
            assertEquals("Hackathon 2025", info.getName());
            assertEquals("AI Challenge", info.getType());
            assertEquals("Generative AI", info.getSubtype());
            assertEquals(5000.0, info.getPrize());

            assertEquals("Offline", info.getLocation());
        }

        @Test
        void shouldThrowExceptionForEmptyName() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Info("", "Type", "Loc", 1, false));
        }

        @Test
        void shouldThrowExceptionForNullName() {
            assertThrows(IllegalArgumentException.class, () ->
                    new Info(null, "Type", "Loc", 1, false));
        }
    }

    @Nested
    class PeriodTests {

        @Test
        void shouldCreateValidPeriod() {
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.plusDays(5);

            Period period = new Period(start, end);

            assertNotNull(period);
            assertEquals(start, period.getStartDate());
            assertEquals(end, period.getEndDate());
        }

        @Test
        void shouldThrowExceptionIfEndBeforeStart() {
            LocalDateTime start = LocalDateTime.now();
            LocalDateTime end = start.minusDays(1);

            assertThrows(IllegalArgumentException.class, () -> new Period(start, end));
        }

        @Test
        void shouldThrowExceptionIfDatesAreNull() {
            assertThrows(IllegalArgumentException.class, () -> new Period(null, LocalDateTime.now()));
            assertThrows(IllegalArgumentException.class, () -> new Period(LocalDateTime.now(), null));
        }
    }

    @Nested
    class RulesTests {

        @Test
        void shouldCreateValidRules() {
            Rules rules = new Rules(4, "https://example.com/rules.pdf");

            assertNotNull(rules);
            assertEquals(4, rules.getMaxTeamSize());
            assertEquals("https://example.com/rules.pdf", rules.getRuleDocument());
        }

        @Test
        void shouldThrowExceptionForInvalidTeamSize() {
            assertThrows(IllegalArgumentException.class, () -> new Rules(0, "doc"));
            assertThrows(IllegalArgumentException.class, () -> new Rules(-1, "doc"));
        }
    }

    @Nested
    class EmailTests {

        @Test
        void shouldCreateValidEmail() {
            Email email = new Email("user@example.com");

            assertNotNull(email);
            assertEquals("user@example.com", email.getAddress());
        }

        @Test
        void shouldThrowExceptionForInvalidFormat() {
            assertThrows(IllegalArgumentException.class, () -> new Email("invalid-email"));
            assertThrows(IllegalArgumentException.class, () -> new Email("user@.com"));
            assertThrows(IllegalArgumentException.class, () -> new Email("@example.com"));
        }

        @Test
        void shouldThrowExceptionForNullOrEmpty() {
            assertThrows(IllegalArgumentException.class, () -> new Email(null));
            assertThrows(IllegalArgumentException.class, () -> new Email(""));
        }
    }

    @Nested
    class GitHubUrlTests {

        @Test
        void shouldCreateValidGitHubUrl() {
            GitHubUrl url = new GitHubUrl("https://github.com/user/repo");

            assertNotNull(url);
            assertEquals("https://github.com/user/repo", url.getUrl());
        }

        @Test
        void shouldThrowExceptionForInvalidPrefix() {
            assertThrows(IllegalArgumentException.class, () -> new GitHubUrl("https://gitlab.com/user/repo"));
        }

        @Test
        void shouldThrowExceptionForNullOrEmpty() {
            assertThrows(IllegalArgumentException.class, () -> new GitHubUrl(null));
            assertThrows(IllegalArgumentException.class, () -> new GitHubUrl(""));
        }
    }

    @Nested
    class UserPasswordTests {

        @Test
        void shouldThrowExceptionForShortPassword() {
            assertThrows(IllegalArgumentException.class, () -> new UserPassword("123"));
        }

        @Test
        void shouldThrowExceptionForNullOrEmpty() {
            assertThrows(IllegalArgumentException.class, () -> new UserPassword(null));
            assertThrows(IllegalArgumentException.class, () -> new UserPassword(""));
        }
    }
}