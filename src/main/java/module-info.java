module com.example.sudokugame {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.sudokugame to javafx.fxml;
    exports com.example.sudokugame;
}