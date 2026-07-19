package com.example.sudokugame.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Main model of the 6x6 Sudoku.
 * Contains the grid, validation rules, the valid-board generator,
 * and the solver (backtracking) needed for the help feature.
 *
 * The board is divided into blocks of 2 rows by 3 columns,
 * each block must contain the numbers 1 to 6 without repeating.
 */
public class SudokuBoard {

    /** Side length of the board. */
    public static final int SIZE = 6;

    /** Number of rows per block. */
    public static final int BOX_ROWS = 2;

    /** Number of columns per block. */
    public static final int BOX_COLS = 3;

    /** Number of fixed hints placed in each 2x3 block at the start. */
    public static final int HINTS_PER_BOX = 2;

    /** How many blocks wide and tall the board has (2 x 3 = 6 blocks total). */
    private static final int BOXES_PER_ROW = SIZE / BOX_COLS;
    private static final int BOXES_PER_COL = SIZE / BOX_ROWS;

    /** Total number of fixed cells at the start: 2 for each of the 6 blocks = 12. */
    public static final int INITIAL_HINTS = HINTS_PER_BOX * BOXES_PER_ROW * BOXES_PER_COL;

    /** Grid of cells. */
    private final Cell[][] grid;

    /** A complete valid solution of the board. Used to give fixed hints and help. */
    private int[][] solution;

    /** Random number generator. */
    private final Random random;

    /**
     * Builds an empty board. {@link #newGame()} must be called
     * to initialize a new game.
     */
    public SudokuBoard() {
        this.grid = new Cell[SIZE][SIZE];
        this.solution = new int[SIZE][SIZE];
        this.random = new Random();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = new Cell(0, false);
            }
        }
    }

    /**
     * Generates a new game: builds a complete valid solution and
     * places {@link #HINTS_PER_BOX} randomly chosen fixed hints within
     * each of the 6 2x3 blocks ({@link #INITIAL_HINTS} in total).
     */
    public void newGame() {
        generateFullSolution();
        placeHints();
    }

    /**
     * Places {@link #HINTS_PER_BOX} random hints in each 2x3 block.
     * Since each block has 6 cells and only 2 are requested, it is
     * always possible to place them: no retry is needed.
     * <p>
     * It is not required that the resulting board have a unique solution:
     * with so few hints (12 of 36 cells) it almost never does. That is why
     * {@link #isSolved()} validates generic compliance with the sudoku
     * rules instead of comparing cell by cell against {@link #solution},
     * so that any valid completion by the player counts as a win,
     * whether or not it matches the solution generated here.
     */
    private void placeHints() {
        clearBoard();
        for (int boxRow = 0; boxRow < BOXES_PER_COL; boxRow++) {
            for (int boxCol = 0; boxCol < BOXES_PER_ROW; boxCol++) {
                int rowStart = boxRow * BOX_ROWS;
                int colStart = boxCol * BOX_COLS;

                List<int[]> boxPositions = new ArrayList<>();
                for (int r = rowStart; r < rowStart + BOX_ROWS; r++) {
                    for (int c = colStart; c < colStart + BOX_COLS; c++) {
                        boxPositions.add(new int[]{r, c});
                    }
                }
                Collections.shuffle(boxPositions, random);

                for (int i = 0; i < HINTS_PER_BOX; i++) {
                    int r = boxPositions.get(i)[0];
                    int c = boxPositions.get(i)[1];
                    grid[r][c] = new Cell(solution[r][c], true);
                }
            }
        }
    }

    /**
     * Clears the board, leaving all cells empty and non-fixed.
     */
    private void clearBoard() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = new Cell(0, false);
            }
        }
    }

    /**
     * Generates a complete valid solution for a 6x6 board
     * with 2x3 blocks.
     */
    private void generateFullSolution() {
        solution = new int[SIZE][SIZE];
        fillSolution(0, 0);
    }

    /**
     * Recursively fills the {@link #solution} matrix by rows
     * using backtracking with random order to vary the solutions.
     *
     * @param row current row.
     * @param col current column.
     * @return true if it managed to complete the solution.
     */
    private boolean fillSolution(int row, int col) {
        if (row == SIZE) {
            return true;
        }
        int nextRow = (col == SIZE - 1) ? row + 1 : row;
        int nextCol = (col + 1) % SIZE;

        List<Integer> numbers = new ArrayList<>();
        for (int n = 1; n <= SIZE; n++) {
            numbers.add(n);
        }
        Collections.shuffle(numbers, random);

        for (int n : numbers) {
            if (isValidPlacement(solution, row, col, n)) {
                solution[row][col] = n;
                if (fillSolution(nextRow, nextCol)) {
                    return true;
                }
                solution[row][col] = 0;
            }
        }
        return false;
    }

    /**
     * Checks whether a number can be placed at a position while
     * respecting the row, column, and 2x3 block rules.
     *
     * @param board board to evaluate.
     * @param row target row.
     * @param col target column.
     * @param number number to place.
     * @return true if the placement is valid.
     */
    private boolean isValidPlacement(int[][] board, int row, int col, int number) {
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == number) {
                return false;
            }
            if (board[i][col] == number) {
                return false;
            }
        }
        int boxRowStart = (row / BOX_ROWS) * BOX_ROWS;
        int boxColStart = (col / BOX_COLS) * BOX_COLS;
        for (int r = boxRowStart; r < boxRowStart + BOX_ROWS; r++) {
            for (int c = boxColStart; c < boxColStart + BOX_COLS; c++) {
                if (board[r][c] == number) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the cell at the indicated position.
     *
     * @param row row.
     * @param col column.
     * @return the corresponding cell.
     */
    public Cell getCell(int row, int col) {
        return grid[row][col];
    }

    /**
     * Gets the value stored in a cell.
     *
     * @param row row.
     * @param col column.
     * @return current value (0 if empty).
     */
    public int getValue(int row, int col) {
        return grid[row][col].getValue();
    }

    /**
     * Gets the value of the internally generated solution for a cell.
     * It is only one of the possible valid solutions; useful for hints
     * and help, but not used to decide whether the player won.
     *
     * @param row row.
     * @param col column.
     * @return value of that reference solution.
     */
    public int getSolutionValue(int row, int col) {
        return solution[row][col];
    }

    /**
     * Places a value in a modifiable cell, only if it is not repeated
     * in its row, column, or 2x3 block relative to the other cells.
     * Accepts any number from 1 to 6 that respects the sudoku rules:
     * it does not require it to match the internally generated solution.
     *
     * @param row row.
     * @param col column.
     * @param number number to place.
     * @return true if it was placed successfully.
     */
    public boolean setValue(int row, int col, int number) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            return false;
        }
        Cell cell = grid[row][col];
        if (cell.isFixed()) {
            return false;
        }
        if (isNumberUsed(row, col, number)) {
            return false;
        }
        cell.setValue(number);
        return true;
    }

    /**
     * Indicates whether a number is already being used by another cell
     * in the same row, column, or 2x3 block. Does not consider the
     * cell itself.
     *
     * @param row row of the target cell.
     * @param col column of the target cell.
     * @param number number to check.
     * @return true if the number is already taken.
     */
    public boolean isNumberUsed(int row, int col, int number) {
        for (int i = 0; i < SIZE; i++) {
            if (i != col && grid[row][i].getValue() == number) {
                return true;
            }
            if (i != row && grid[i][col].getValue() == number) {
                return true;
            }
        }
        int boxRowStart = (row / BOX_ROWS) * BOX_ROWS;
        int boxColStart = (col / BOX_COLS) * BOX_COLS;
        for (int r = boxRowStart; r < boxRowStart + BOX_ROWS; r++) {
            for (int c = boxColStart; c < boxColStart + BOX_COLS; c++) {
                if ((r != row || c != col) && grid[r][c].getValue() == number) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Clears the content of a modifiable cell.
     *
     * @param row row.
     * @param col column.
     */
    public void clearValue(int row, int col) {
        if (row < 0 || row >= SIZE || col < 0 || col >= SIZE) {
            return;
        }
        Cell cell = grid[row][col];
        if (!cell.isFixed()) {
            cell.setValue(0);
        }
    }

    /**
     * Indicates whether the current value of the cell matches the
     * internally generated solution. Kept only as a reference; NOT
     * used to decide victory (see {@link #isSolved()}), because with
     * only 12 hints there may be more than one valid completion.
     *
     * @param row row.
     * @param col column.
     * @return true if it matches, false otherwise or if it is empty.
     */
    public boolean isCorrect(int row, int col) {
        if (grid[row][col].isEmpty()) {
            return false;
        }
        return grid[row][col].getValue() == solution[row][col];
    }

    /**
     * Indicates whether the board is complete and is a valid sudoku
     * solution: each row, column, and 2x3 block contains the numbers
     * 1 to 6 without repeating. Validated generically instead of
     * comparing against the internally generated solution, so that
     * any correct completion by the player counts as a win.
     *
     * @return true if the player has won.
     */
    public boolean isSolved() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c].isEmpty()) {
                    return false;
                }
            }
        }
        for (int r = 0; r < SIZE; r++) {
            if (!isGroupValid(rowValues(r))) {
                return false;
            }
        }
        for (int c = 0; c < SIZE; c++) {
            if (!isGroupValid(colValues(c))) {
                return false;
            }
        }
        for (int boxRow = 0; boxRow < BOXES_PER_COL; boxRow++) {
            for (int boxCol = 0; boxCol < BOXES_PER_ROW; boxCol++) {
                int[] values = boxValues(boxRow * BOX_ROWS, boxCol * BOX_COLS);
                if (!isGroupValid(values)) {
                    return false;
                }
            }
        }
        return true;
    }

    private int[] rowValues(int row) {
        int[] values = new int[SIZE];
        for (int c = 0; c < SIZE; c++) {
            values[c] = grid[row][c].getValue();
        }
        return values;
    }

    private int[] colValues(int col) {
        int[] values = new int[SIZE];
        for (int r = 0; r < SIZE; r++) {
            values[r] = grid[r][col].getValue();
        }
        return values;
    }

    private int[] boxValues(int rowStart, int colStart) {
        int[] values = new int[SIZE];
        int i = 0;
        for (int r = rowStart; r < rowStart + BOX_ROWS; r++) {
            for (int c = colStart; c < colStart + BOX_COLS; c++) {
                values[i++] = grid[r][c].getValue();
            }
        }
        return values;
    }

    /**
     * Checks that a group of {@link #SIZE} values contains each
     * number from 1 to 6 exactly once.
     */
    private boolean isGroupValid(int[] values) {
        boolean[] seen = new boolean[SIZE + 1];
        for (int v : values) {
            if (v < 1 || v > SIZE || seen[v]) {
                return false;
            }
            seen[v] = true;
        }
        return true;
    }

    /**
     * Counts how many empty cells remain. Used to determine whether
     * the help feature should be disabled when only one cell is left
     * to fill.
     *
     * @return number of empty cells.
     */
    public int countEmpty() {
        int count = 0;
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c].isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Looks for an empty cell that can still be filled with at least
     * one valid number according to the current state of the board
     * (not just the internally generated solution).
     *
     * @return coordinates of the cell to fill, or null if there are
     *         no candidate cells.
     */
    public int[] pickHelpCell() {
        List<int[]> candidates = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c].isEmpty() && findAnyValidNumber(r, c) != 0) {
                    candidates.add(new int[]{r, c});
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        }
        Collections.shuffle(candidates, random);
        return candidates.get(0);
    }

    /**
     * Fills the indicated cell with a valid value: preferably the one
     * from the internally generated solution, or if that one already
     * conflicts with the user's previous moves (because they followed
     * an equally valid but different path), any other number that
     * respects the rules for that cell.
     *
     * @param row row.
     * @param col column.
     */
    public void applyHelp(int row, int col) {
        if (grid[row][col].isFixed() || !grid[row][col].isEmpty()) {
            return;
        }
        int candidate = solution[row][col];
        if (isNumberUsed(row, col, candidate)) {
            candidate = findAnyValidNumber(row, col);
        }
        if (candidate != 0) {
            grid[row][col].setValue(candidate);
        }
    }

    /**
     * Looks for any number from 1 to 6 that can be placed in the
     * indicated cell without violating the row, column, or block rules.
     *
     * @param row row.
     * @param col column.
     * @return a valid number, or 0 if none is valid.
     */
    private int findAnyValidNumber(int row, int col) {
        for (int n = 1; n <= SIZE; n++) {
            if (!isNumberUsed(row, col, n)) {
                return n;
            }
        }
        return 0;
    }

    /**
     * Checks whether the current placement respects the sudoku rules.
     * Used to detect and highlight errors.
     *
     * @param row row.
     * @param col column.
     * @return true if the current value violates any rule.
     */
    public boolean hasConflict(int row, int col) {
        int value = grid[row][col].getValue();
        if (value == 0) {
            return false;
        }
        for (int i = 0; i < SIZE; i++) {
            if (i != col && grid[row][i].getValue() == value) {
                return true;
            }
            if (i != row && grid[i][col].getValue() == value) {
                return true;
            }
        }
        int boxRowStart = (row / BOX_ROWS) * BOX_ROWS;
        int boxColStart = (col / BOX_COLS) * BOX_COLS;
        for (int r = boxRowStart; r < boxRowStart + BOX_ROWS; r++) {
            for (int c = boxColStart; c < boxColStart + BOX_COLS; c++) {
                if ((r != row || c != col) && grid[r][c].getValue() == value) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a text representation of the board, useful for
     * debugging.
     *
     * @return string with the board's contents.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                sb.append(grid[r][c].getValue());
                if (c < SIZE - 1) {
                    sb.append(" ");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}