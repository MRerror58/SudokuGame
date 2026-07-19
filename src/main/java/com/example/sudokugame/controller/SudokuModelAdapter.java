package com.example.sudokugame.controller;
 
/**
 * Contract that an adapter between the Sudoku model and the view must fulfill.
 * Allows decoupling the presentation layer from the model layer,
 * following the MVC pattern and separation of concerns.
 */
public interface SudokuModelAdapter {
 
    /**
     * Requests a restart of the game. May show a confirmation
     * before proceeding.
     */
    void requestRestart();
 
    /**
     * Requests help from the system. Fills a random valid cell.
     */
    void requestHelp();
 
    /**
     * Requests that a number be written into a specific cell.
     *
     * @param row row of the cell.
     * @param col column of the cell.
     * @param number number to write (1 to 6).
     */
    void requestNumber(int row, int col, int number);
}