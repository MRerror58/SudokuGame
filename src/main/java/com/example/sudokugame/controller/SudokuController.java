package com.example.sudokugame.controller;

import com.example.sudokugame.model.Cell;
import com.example.sudokugame.model.SudokuBoard;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.Optional;

/**
 * Controller for the Sudoku view.
 *
 * Implements the {@link SudokuModelAdapter} interface to act as an
 * adapter between the view (FXML) and the model ({@link SudokuBoard}),
 * following the MVC architecture.
 *
 * Responsible for:
 * <ul>
 *   <li>Building the 6x6 grid from the model.</li>
 *   <li>Handling mouse events (cell selection) and
 *       keyboard events (entering numbers 1 to 6 or deleting).</li>
 *   <li>Validating and highlighting incorrect numbers in red, with a
 *       "shake" animation that indicates exactly where the error
 *       occurred, and a color pulse when correct.</li>
 *   <li>Controlling the help button and game restarts.</li>
 * </ul>
 */
public class SudokuController implements SudokuModelAdapter {

    /** Size in pixels of each board cell. */
    private static final double CELL_SIZE = 82;

    /** Soft color used for the divider lines of the 2x3 blocks. */
    private static final Color GRID_LINE_COLOR = Color.web("#4A4A68");

    /** Thickness of the block divider lines. */
    private static final double GRID_LINE_WIDTH = 3.5;

    /** Corner radius of the board's outer border. */
    private static final double BOARD_CORNER_RADIUS = 26;

    /** Game model. */
    private final SudokuBoard board;

    /** Matrix of text fields representing the cells. */
    private final TextField[][] cells;

    /** Currently selected row. */
    private int selectedRow = -1;

    /** Currently selected column. */
    private int selectedCol = -1;

    @FXML
    private GridPane boardGrid;

    @FXML
    private javafx.scene.layout.StackPane boardContainer;

    @FXML
    private Label statusLabel;

    @FXML
    private Label feedbackLabel;

    @FXML
    private Button newGameButton;

    @FXML
    private Button helpButton;

    @FXML
    private Button clearButton;

    /**
     * Builds a controller and prepares the internal structures.
     */
    public SudokuController() {
        this.board = new SudokuBoard();
        this.cells = new TextField[SudokuBoard.SIZE][SudokuBoard.SIZE];
    }

    /**
     * Initialization method called by JavaFX when the FXML is loaded.
     * Builds the visual grid, wires up the event handlers,
     * and starts a new game.
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
     * Builds the 6x6 grid of text fields and visually divides
     * it into 2x3 blocks using thick lines.
     */
    private void buildBoard() {
        boardGrid.getChildren().clear();
        boardGrid.setHgap(0);
        boardGrid.setVgap(0);
        boardGrid.setPadding(Insets.EMPTY);
        boardGrid.setAlignment(Pos.CENTER);

        double cellSize = CELL_SIZE;
        double totalSize = cellSize * SudokuBoard.SIZE;

        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            for (int c = 0; c < SudokuBoard.SIZE; c++) {
                TextField tf = new TextField();
                tf.setPrefSize(cellSize, cellSize);
                tf.setMaxSize(cellSize, cellSize);
                tf.setMinSize(cellSize, cellSize);
                tf.setAlignment(Pos.CENTER);
                tf.setFont(Font.font("Verdana", FontWeight.BOLD, 26));
                tf.setEditable(false);
                tf.getStyleClass().add("sudoku-cell");

                final int row = r;
                final int col = c;
                tf.setOnMouseClicked((MouseEvent e) -> selectCell(row, col));
                cells[r][c] = tf;
                boardGrid.add(tf, c, r);
            }
        }

        // Canvas that draws the thick borders of the 2x3 blocks.
        // Added to the same StackPane and positioned at (0,0) so
        // its coordinates line up exactly with the cells.
        Pane overlay = new Pane();
        overlay.setMouseTransparent(true);
        overlay.setPrefSize(totalSize, totalSize);
        overlay.setMaxSize(totalSize, totalSize);
        overlay.setMinSize(totalSize, totalSize);

        // The inner dividers of the 2x3 blocks are shortened slightly and
        // use rounded caps so they don't end at right angles
        // against the curved outer border.
        double lineInset = GRID_LINE_WIDTH / 2.0;
        for (int i = 1; i < SudokuBoard.SIZE; i++) {
            if (i % SudokuBoard.BOX_COLS == 0) {
                Line v = new Line(i * cellSize, lineInset, i * cellSize, totalSize - lineInset);
                v.setStrokeWidth(GRID_LINE_WIDTH);
                v.setStroke(GRID_LINE_COLOR);
                v.setStrokeLineCap(StrokeLineCap.ROUND);
                overlay.getChildren().add(v);
            }
            if (i % SudokuBoard.BOX_ROWS == 0) {
                Line h = new Line(lineInset, i * cellSize, totalSize - lineInset, i * cellSize);
                h.setStrokeWidth(GRID_LINE_WIDTH);
                h.setStroke(GRID_LINE_COLOR);
                h.setStrokeLineCap(StrokeLineCap.ROUND);
                overlay.getChildren().add(h);
            }
        }

        // Outer border with rounded corners, instead of a straight
        // rectangle, to match the rest of the pastel aesthetic.
        Rectangle outerBorder = new Rectangle(0, 0, totalSize, totalSize);
        outerBorder.setFill(Color.TRANSPARENT);
        outerBorder.setStroke(GRID_LINE_COLOR);
        outerBorder.setStrokeWidth(GRID_LINE_WIDTH);
        outerBorder.setStrokeType(StrokeType.INSIDE);
        outerBorder.setArcWidth(BOARD_CORNER_RADIUS * 2);
        outerBorder.setArcHeight(BOARD_CORNER_RADIUS * 2);
        overlay.getChildren().add(outerBorder);

        // Captures keys at the grid level, globally.
        EventHandler<KeyEvent> keyHandler = this::handleKey;
        for (int r = 0; r < SudokuBoard.SIZE; r++) {
            for (int c = 0; c < SudokuBoard.SIZE; c++) {
                cells[r][c].setOnKeyPressed(keyHandler);
            }
        }

        // Place the grid and overlay together in a panel of the same size
        // so their coordinates match, and clip that panel with a
        // rounded-corner rectangle: this way the cells in the four
        // corners also follow the curved outline instead of having their
        // straight corners poke out past the border.
        boardGrid.setPrefSize(totalSize, totalSize);
        boardGrid.setMaxSize(totalSize, totalSize);
        boardGrid.setMinSize(totalSize, totalSize);

        javafx.scene.layout.StackPane gridArea = new javafx.scene.layout.StackPane(boardGrid, overlay);
        gridArea.setPrefSize(totalSize, totalSize);
        gridArea.setMaxSize(totalSize, totalSize);
        gridArea.setMinSize(totalSize, totalSize);

        Rectangle clip = new Rectangle(totalSize, totalSize);
        clip.setArcWidth(BOARD_CORNER_RADIUS * 2);
        clip.setArcHeight(BOARD_CORNER_RADIUS * 2);
        gridArea.setClip(clip);

        boardContainer.getChildren().clear();
        boardContainer.getChildren().add(gridArea);
    }

    /**
     * Keyboard handler. Only responds if a cell is selected.
     * Accepts numbers 1 to 6 and deletion with Backspace or Delete.
     *
     * @param event keyboard event.
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
     * Selects a cell and visually marks it.
     *
     * @param row selected row.
     * @param col selected column.
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
        clearFeedback();
    }

    /**
     * Refreshes all the view's cells from the model.
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
     * Visually refreshes an individual cell: text, color, and
     * state (fixed, editable, error).
     *
     * @param row row.
     * @param col column.
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
     * Updates the status label and the availability of the help
     * button based on the game state.
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
     * Shows a feedback message next to the board, with a different
     * color depending on the type (error, success, or info).
     *
     * @param message text to display.
     * @param styleClass one of "feedback-error", "feedback-success", or "feedback-info".
     */
    private void showFeedback(String message, String styleClass) {
        feedbackLabel.getStyleClass().removeAll("feedback-error", "feedback-success", "feedback-info");
        feedbackLabel.getStyleClass().add(styleClass);
        feedbackLabel.setText(message);
    }

    /**
     * Clears the feedback message.
     */
    private void clearFeedback() {
        feedbackLabel.getStyleClass().removeAll("feedback-error", "feedback-success", "feedback-info");
        feedbackLabel.setText("");
    }

    /**
     * Plays a brief "shake" animation on a cell to visually indicate,
     * in addition to the red color, where an input error occurred.
     *
     * @param tf text field of the cell to shake.
     */
    private void shakeCell(TextField tf) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(55), tf);
        shake.setByX(8);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> tf.setTranslateX(0));
        shake.play();
    }

    /**
     * Plays a small scale pulse on a cell to visually reinforce
     * a correct answer or a placed hint.
     *
     * @param tf text field of the cell to highlight.
     */
    private void pulseCell(TextField tf) {
        ScaleTransition pulse = new ScaleTransition(Duration.millis(130), tf);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.15);
        pulse.setToY(1.15);
        pulse.setCycleCount(2);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    /**
     * Clears the content of the selected cell if it is modifiable.
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
        clearFeedback();
    }

    // -----------------------------------------------------------------
    // Implementation of the SudokuModelAdapter interface
    // -----------------------------------------------------------------

    /**
     * {@inheritDoc}
     * Shows a confirmation dialog before restarting the game.
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
            clearFeedback();
        }
    }

    /**
     * {@inheritDoc}
     * Fills an empty cell with a valid value from the solution.
     * Disabled when only one cell is left to complete.
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
        showFeedback("Pista colocada en la celda seleccionada.", "feedback-info");
        pulseCell(cells[pos[0]][pos[1]]);
        if (board.isSolved()) {
            showInfo("Victoria", "Has completado el sudoku.");
        }
    }

    /**
     * {@inheritDoc}
     * Places a number in the indicated cell. If the number is already
     * used in the same row, column, or block, the action is rejected,
     * the user is informed with a red message, and the cell is
     * shaken to make clear where the error occurred. If the number is
     * valid, the message switches to a green tone and the cell pulses
     * briefly to confirm the correct move.
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
            showFeedback("El " + number + " ya esta en esa fila, columna o bloque.", "feedback-error");
            shakeCell(cells[row][col]);
            return;
        }
        board.setValue(row, col, number);
        refreshCell(row, col);
        updateStatus();
        showFeedback("Bien! " + number + " colocado correctamente.", "feedback-success");
        pulseCell(cells[row][col]);
        if (board.isSolved()) {
            showInfo("Victoria", "Has completado el sudoku.");
        }
    }

    /**
     * Shows a simple informational dialog.
     *
     * @param title dialog title.
     * @param message message to display.
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}