package com.example.sudokugame.model;

/**
 * Represents an individual cell of the 6x6 Sudoku board.
 * Each cell stores a numeric value between 1 and 6 (or 0 if empty)
 * and keeps track of whether the value was placed initially
 * or entered by the user.
 */
public class Cell {

    /** Current value of the cell. 0 indicates an empty cell. */
    private int value;

    /** Indicates whether the cell is a fixed value from the initial board. */
    private final boolean fixed;

    /**
     * Builds a cell with a value and a fixed state.
     *
     * @param value initial value (0 if empty).
     * @param fixed true if the cell belongs to the initial board.
     */
    public Cell(int value, boolean fixed) {
        this.value = value;
        this.fixed = fixed;
    }

    /**
     * Gets the current value of the cell.
     *
     * @return the stored value (0 if empty).
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets a new value for the cell.
     *
     * @param value new value (0 to 6). 0 represents an empty cell.
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Indicates whether the cell is a fixed value from the initial board.
     *
     * @return true if the user cannot modify this cell.
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * Indicates whether the cell is empty.
     *
     * @return true if the value is 0.
     */
    public boolean isEmpty() {
        return value == 0;
    }
}