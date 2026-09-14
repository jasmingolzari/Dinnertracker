module no.ntnu.it1901.gr2631.dinnertracker.ui {
    requires javafx.controls;
    requires javafx.fxml;
    requires no.ntnu.it1901.gr2631.dinnertracker.core;
    requires no.ntnu.it1901.gr2631.dinnertracker.persistence;

    exports no.ntnu.it1901.gr2631.dinnertracker.ui;
    opens no.ntnu.it1901.gr2631.dinnertracker.ui to javafx.fxml;
}