package no.gr2631.dinnertracker.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Tests that users retain the username supplied at creation. */
public class UserTest {
  @Test
  void testUserCreation() {
    User user = new User("testuser");

    assertEquals("testuser", user.getUserName());
  }

  @Test
  void testEmptyUserName() {
    User user = new User("");

    assertEquals("", user.getUserName());
  }

  @Test
  void testBlankUserName() {
    User user = new User("   ");

    assertEquals("   ", user.getUserName());
  }

  @Test
  void testNullUserName() {
    User user = new User(null);

    assertNull(user.getUserName());
  }
}
