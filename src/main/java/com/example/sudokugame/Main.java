package com.example.sudokugame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Entry point of the JavaFX application.
 * Loads the view defined in {@code sudoku-view.fxml} and applies
 * the {@code sudoku.css} stylesheet.
 */
public class Main extends Application {

    /**
     * Starts the application's main window.
     *
     * @param stage the main JavaFX stage.
     * @throws IOException if the FXML file cannot be loaded.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(Main.class.getResource("view/sudoku-view.fxml")));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(Main.class.getResource("view/sudoku.css"))
                        .toExternalForm());
        stage.setTitle("SudokuGame");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}