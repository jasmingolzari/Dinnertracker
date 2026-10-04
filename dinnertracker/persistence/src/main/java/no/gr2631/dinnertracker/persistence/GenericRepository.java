package no.gr2631.dinnertracker.persistence;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
  List<T> loadAll(Path path) throws IOException;

  /**
   * Loads the entity identified by the given identifier.
   *
   * @param path the path to read from
   * @param identifier the identifier of the entity to load
   * @return the matching entity
   * @throws IOException if the entities cannot be read
   */
  T load(Path path, String identifier) throws IOException;

  /**
   * Writes all entities to the given path, replacing any existing content.
   *
   * @param entities the entities to save
   * @param path the path to write to
   * @throws IOException if the entities cannot be written
   */
  void saveAll(List<T> entities, Path path) throws IOException;
}
