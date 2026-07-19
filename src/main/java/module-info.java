module com.example.sudokugame {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.sudokugame to javafx.fxml;
    exports com.example.sudokugame;
    exports com.example.sudokugame.controller;
    opens com.example.sudokugame.controller to javafx.fxml;
    exports com.example.sudokugame.model;
    opens com.example.sudokugame.model to javafx.fxml;
}