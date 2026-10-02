module no.gr2631.dinnertracker.ui {
  requires javafx.controls;
  requires javafx.fxml;
  requires transitive javafx.graphics;
  requires org.kordamp.ikonli.javafx;
  requires org.kordamp.ikonli.materialdesign2;
  requires transitive no.gr2631.dinnertracker.core;
  requires no.gr2631.dinnertracker.persistence;

  exports no.gr2631.dinnertracker.ui;

  opens no.gr2631.dinnertracker.ui to
      javafx.fxml;
}
