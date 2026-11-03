// The module name follows the project group ID.
module no.gr2631.dinnertracker.persistence {
  requires transitive no.gr2631.dinnertracker.core;
  requires com.fasterxml.jackson.databind;

  exports no.gr2631.dinnertracker.persistence;
}
