package no.gr2631.dinnertracker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Tests that users retain the username supplied at creation. */
public class UserTest {
  /** Tests that a user returns the username it was created with. */
  @Test
  void testUserCreation() {
    User user = new User("testuser");

    assertEquals("testuser", user.getUserName());
  }

  /** Tests that an empty username is stored as it is, since {@link User} does no validation. */
  @Test
  void testEmptyUserName() {
    User user = new User("");

    assertEquals("", user.getUserName());
  }

  /** Tests that a username of only spaces is stored as it is, without being trimmed. */
  @Test
  void testBlankUserName() {
    User user = new User("   ");

    assertEquals("   ", user.getUserName());
  }

  /** Tests that a user created with a {@code null} username returns {@code null}. */
  @Test
  void testNullUserName() {
    User user = new User(null);

    assertNull(user.getUserName());
  }
}
