package com.example.sudokugame;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Punto de entrada de la aplicacion JavaFX.
 * Carga la vista definida en {@code sudoku-view.fxml} y aplica
 * la hoja de estilos {@code sudoku.css}.
 */
public class Main extends Application {

    /**
     * Inicia la ventana principal de la aplicacion.
     *
     * @param stage escenario principal de JavaFX.
     * @throws IOException si no se puede cargar el archivo FXML.
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Objects.requireNonNull(Main.class.getResource("sudoku-view.fxml")));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(Main.class.getResource("sudoku.css"))
                        .toExternalForm());
        stage.setTitle("Sudoku 6x6");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
}
