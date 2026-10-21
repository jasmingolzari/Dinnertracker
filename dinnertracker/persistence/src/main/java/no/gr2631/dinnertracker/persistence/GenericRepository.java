package no.gr2631.dinnertracker.persistence;

import java.io.IOException;
import java.nio.file.Path;
import no.gr2631.dinnertracker.core.User;

/**
 * Provides file-based persistence operations for entities of a specific type. (In our case it is
 * RecipeFileHandler and FridgeFileHandler)
 */
public interface GenericRepository<T> {
  /**
   * Loads all entities from the given path.
   *
   * @param path the path to read from
   * @return all entities stored at the path
   * @throws IOException if the entities cannot be read
   */
  T load(Path path, User userName) throws IOException;

  /**
   * Writes all entities to the given path, replacing any existing content.
   *
   * @param entities the entities to save
   * @param path the path to write to
   * @throws IOException if the entities cannot be written
   */
  void save(Path path, T entities) throws IOException;
}
