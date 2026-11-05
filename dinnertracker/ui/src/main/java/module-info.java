// The module name follows the project group ID.
module no.gr2631.dinnertracker.ui {
  requires static com.github.spotbugs.annotations;
  requires com.fasterxml.jackson.databind;
  requires java.net.http;
  requires javafx.controls;
  requires javafx.fxml;
  requires transitive javafx.graphics;
  requires org.kordamp.ikonli.javafx;
  requires org.kordamp.ikonli.materialdesign2;
  requires transitive no.gr2631.dinnertracker.core;

  exports no.gr2631.dinnertracker.ui;

  opens no.gr2631.dinnertracker.ui to
      javafx.fxml;
}
