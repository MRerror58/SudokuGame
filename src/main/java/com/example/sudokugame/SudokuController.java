package com.example.sudokugame;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

import java.util.Optional;

/**
 * Controlador de la vista del Sudoku.
 *
 * Implementa la interfaz {@link SudokuModelAdapter} para actuar como
 * adaptador entre la vista (FXML) y el modelo ({@link SudokuBoard}),
 * siguiendo la arquitectura MVC.
 *
 * Se encarga de:
 * <ul>
 *   <li>Construir la cuadricula 6x6 a partir del modelo.</li>
 *   <li>Gestionar los eventos de mouse (seleccion de celda) y
 *       teclado (escritura de numeros del 1 al 6 o borrado).</li>
 *   <li>Validar y resaltar en rojo los numeros incorrectos.</li>
 *   <li>Controlar el boton de ayuda y los reinicios de partida.</li>
 * </ul>
 */
public class SudokuController implements SudokuModelAdapter {

    /** Modelo del juego. */
    private final SudokuBoard board;

    /** Matriz de campos de texto que representan las celdas. */
    private final TextField[][] cells;

    /** Fila actualmente seleccionada. */
    private int selectedRow = -1;

    /** Columna actualmente seleccionada. */
    private int selectedCol = -1;

    @FXML
    private GridPane boardGrid;

    @FXML
    private javafx.scene.layout.StackPane boardContainer;

    @FXML
    private Label statusLabel;

    @FXML
    private Button newGameButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button clearButton;

    /**
     * Construye un controlador y prepara las estructuras internas.
     */
    public SudokuController() {
        this.board = new SudokuBoard();
        this.cells = new TextField[SudokuBoard.SIZE][SudokuBoard.SIZE];
    }

    /**
     * Metodo de inicializacion llamado por JavaFX al cargar el FXML.
     * Construye la cuadricula visual, conecta los manejadores de
     * eventos y arranca un juego nuevo.
     */
    @FXML
    public void initialize() {
        buildBoard();
        newGameButton.setOnAction(e -> requestRestart());
        helpButton.setOnAction(e -> requestHelp());
        clearButton.setOnAction(e -> clearSelected());
        board.newGame();
        refreshAll();
    }

    /**
     * Construye la cuadricula 6x6 de campos de texto y la divide
     * visualmente en bloques 2x3 mediante lineas gruesas.
     */
    private void buildBoard() {
        boardGrid.getChildren().clear();
        boardGrid.setHgap(0);
        boardGrid.setVgap(0);
        boardGrid.setPadding(Insets.EMPTY);
        boardGrid.setAlignment(Pos.CENTER);

        double cellSize = 64;
        double totalSize = cellSize * SudokuBoard.SIZE;

        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            for (int c = 0; c < SudokuBoard.SIZE; c++) {
                TextField tf = new TextField();
                tf.setPrefSize(cellSize, cellSize);
                tf.setMaxSize(cellSize, cellSize);
                tf.setMinSize(cellSize, cellSize);
                tf.setAlignment(Pos.CENTER);
                tf.setFont(Font.font("Monospaced", 22));
                tf.setEditable(false);
                tf.getStyleClass().add("sudoku-cell");

                final int row = r;
                final int col = c;
                tf.setOnMouseClicked((MouseEvent e) -> selectCell(row, col));
                cells[r][c] = tf;
                boardGrid.add(tf, c, r);
            }
        }

        // Lienzo que dibuja los bordes gruesos de los bloques 2x3.
        // Se añade al mismo StackPane y se posiciona en (0,0) para
        // que sus coordenadas coincidan exactamente con las celdas.
        Pane overlay = new Pane();
        overlay.setMouseTransparent(true);
        overlay.setPrefSize(totalSize, totalSize);
        overlay.setMaxSize(totalSize, totalSize);
        overlay.setMinSize(totalSize, totalSize);

        for (int i = 1; i < SudokuBoard.SIZE; i++) {
            boolean thickBox = (i % SudokuBoard.BOX_COLS == 0)
                    || (i % SudokuBoard.BOX_ROWS == 0);
            if (i % SudokuBoard.BOX_COLS == 0) {
                Line v = new Line(i * cellSize, 0, i * cellSize, totalSize);
                v.setStrokeWidth(thickBox ? 3 : 1);
                v.setStroke(Color.BLACK);
                overlay.getChildren().add(v);
            }
            if (i % SudokuBoard.BOX_ROWS == 0) {
                Line h = new Line(0, i * cellSize, totalSize, i * cellSize);
                h.setStrokeWidth(thickBox ? 3 : 1);
                h.setStroke(Color.BLACK);
                overlay.getChildren().add(h);
            }
        }
        // Borde exterior.
        Line leftLine = new Line(0, 0, 0, totalSize);
        leftLine.setStrokeWidth(3);
        Line rightLine = new Line(totalSize, 0, totalSize, totalSize);
        rightLine.setStrokeWidth(3);
        Line topLine = new Line(0, 0, totalSize, 0);
        topLine.setStrokeWidth(3);
        Line bottomLine = new Line(0, totalSize, totalSize, totalSize);
        bottomLine.setStrokeWidth(3);
        overlay.getChildren().addAll(leftLine, rightLine, topLine, bottomLine);

        // Captura teclas a nivel global de la cuadricula.
        EventHandler<KeyEvent> keyHandler = this::handleKey;
        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            for (int c = 0; c < SudokuBoard.SIZE; c++) {
                cells[r][c].setOnKeyPressed(keyHandler);
            }
        }

        // Colocar cuadricula y overlay en el StackPane con el mismo tamano
        // para que las coordenadas coincidan.
        boardContainer.getChildren().clear();
        boardGrid.setPrefSize(totalSize, totalSize);
        boardGrid.setMaxSize(totalSize, totalSize);
        boardGrid.setMinSize(totalSize, totalSize);
        boardContainer.getChildren().addAll(boardGrid, overlay);
    }

    /**
     * Manejador de teclado. Solo responde si hay una celda seleccionada.
     * Acepta numeros del 1 al 6 y borrado con Backspace o Delete.
     *
     * @param event evento de teclado.
     */
    private void handleKey(KeyEvent event) {
        if (selectedRow < 0 || selectedCol < 0) {
            return;
        }
        String key = event.getText();
        if (key == null || key.isEmpty()) {
            return;
        }
        char ch = key.charAt(0);
        if (ch >= '1' && ch <= '6') {
            int number = Character.getNumericValue(ch);
            requestNumber(selectedRow, selectedCol, number);
            event.consume();
        } else if (event.getCode() == javafx.scene.input.KeyCode.BACK_SPACE
                || event.getCode() == javafx.scene.input.KeyCode.DELETE
                || ch == '0') {
            clearSelected();
            event.consume();
        }
    }

    /**
     * Selecciona una celda y la marca visualmente.
     *
     * @param row fila seleccionada.
     * @param col columna seleccionada.
     */
    private void selectCell(int row, int col) {
        if (row < 0 || row >= SudokuBoard.SIZE || col < 0 || col >= SudokuBoard.SIZE) {
            return;
        }
        if (selectedRow >= 0 && selectedCol >= 0) {
            cells[selectedRow][selectedCol].getStyleClass().remove("selected");
        }
        selectedRow = row;
        selectedCol = col;
        cells[row][col].getStyleClass().add("selected");
        cells[row][col].requestFocus();
    }

    /**
     * Actualiza todas las celdas de la vista a partir del modelo.
     */
    private void refreshAll() {
        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            for (int c = 0; c < SudokuBoard.SIZE; c++) {
                refreshCell(r, c);
            }
        }
        updateStatus();
    }

    /**
     * Actualiza visualmente una celda individual: texto, color y
     * estado (fija, editable, error).
     *
     * @param row fila.
     * @param col columna.
     */
    private void refreshCell(int row, int col) {
        Cell cell = board.getCell(row, col);
        TextField tf = cells[row][col];
        tf.getStyleClass().removeAll("fixed", "error", "user");
        if (cell.isFixed()) {
            tf.getStyleClass().add("fixed");
            tf.setEditable(false);
        } else {
            tf.setEditable(false);
            if (!cell.isEmpty()) {
                tf.getStyleClass().add("user");
            }
        }
        tf.setText(cell.isEmpty() ? "" : Integer.toString(cell.getValue()));
        if (!cell.isEmpty() && board.hasConflict(row, col)) {
            tf.getStyleClass().add("error");
        }
    }

    /**
     * Actualiza la etiqueta de estado y la disponibilidad del boton
     * de ayuda segun el estado del juego.
     */
    private void updateStatus() {
        if (board.isSolved()) {
            statusLabel.setText("Has ganado!");
            helpButton.setDisable(true);
        } else {
            int empty = board.countEmpty();
            statusLabel.setText("Celdas vacias: " + empty);
            helpButton.setDisable(empty <= 1);
        }
    }

    /**
     * Borra el contenido de la celda seleccionada si esta es modificable.
     */
    private void clearSelected() {
        if (selectedRow < 0 || selectedCol < 0) {
            return;
        }
        if (board.getCell(selectedRow, selectedCol).isFixed()) {
            return;
        }
        board.clearValue(selectedRow, selectedCol);
        refreshCell(selectedRow, selectedCol);
        updateStatus();
    }

    // -----------------------------------------------------------------
    // Implementacion de la interfaz SudokuModelAdapter
    // -----------------------------------------------------------------

    /**
     * {@inheritDoc}
     * Muestra un dialogo de confirmacion antes de reiniciar el juego.
     */
    @Override
    public void requestRestart() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reiniciar juego");
        alert.setHeaderText(null);
        alert.setContentText("Deseas iniciar un nuevo juego? Se perdera el progreso actual.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            board.newGame();
            selectedRow = -1;
            selectedCol = -1;
            refreshAll();
        }
    }

    /**
     * {@inheritDoc}
     * Rellena una celda vacia con el valor correcto de la solucion.
     * Se desactiva cuando solo queda una celda por completar.
     */
    @Override
    public void requestHelp() {
        if (board.countEmpty() <= 1) {
            return;
        }
        int[] pos = board.pickHelpCell();
        if (pos == null) {
            return;
        }
        board.applyHelp(pos[0], pos[1]);
        refreshCell(pos[0], pos[1]);
        selectCell(pos[0], pos[1]);
        updateStatus();
        if (board.isSolved()) {
            showInfo("Victoria", "Has completado el sudoku.");
        }
    }

    /**
     * {@inheritDoc}
     * Coloca un numero en la celda indicada. Si el numero ya esta
     * usado en la misma fila, columna o bloque, la accion se rechaza
     * y se informa al usuario.
     */
    @Override
    public void requestNumber(int row, int col, int number) {
        if (number < 1 || number > SudokuBoard.SIZE) {
            return;
        }
        Cell cell = board.getCell(row, col);
        if (cell.isFixed()) {
            return;
        }
        if (board.isNumberUsed(row, col, number)) {
            statusLabel.setText("El numero " + number
                    + " ya esta en la fila, columna o bloque.");
            return;
        }
        board.setValue(row, col, number);
        refreshCell(row, col);
        updateStatus();
        if (board.isSolved()) {
            showInfo("Victoria", "Has completado el sudoku.");
        }
    }

    /**
     * Muestra un dialogo informativo simple.
     *
     * @param title titulo del dialogo.
     * @param message mensaje a mostrar.
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
