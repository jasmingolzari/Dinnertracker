package no.gr2631.dinnertracker.core;

/** Represents a user identified by a username. */
public class User {
  private final String userName;

  /**
   * Creates a user with the given username. The username is stored without validation.
   *
   * @param userName the username, which may be null or blank
   */
  public User(String userName) {
    this.userName = userName;
  }

  /**
   * Gets the username supplied when this user was created.
   *
   * @return the username, or null if no username was supplied
   */
  public String getUserName() {
    return userName;
  }
}
