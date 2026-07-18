package com.example.sudokugame;

/**
 * Contrato que un adaptador entre el modelo de Sudoku y la vista debe cumplir.
 * Permite desacoplar la capa de presentacion de la capa de modelo,
 * cumpliendo con el patron MVC y la separacion de responsabilidades.
 */
public interface SudokuModelAdapter {

    /**
     * Solicita el reinicio del juego. Puede mostrar una confirmacion
     * antes de proceder.
     */
    void requestRestart();

    /**
     * Solicita la ayuda del sistema. Rellena una celda aleatoria valida.
     */
    void requestHelp();

    /**
     * Solicita la escritura de un numero en una celda especifica.
     *
     * @param row fila de la celda.
     * @param col columna de la celda.
     * @param number numero a escribir (1 a 6).
     */
    void requestNumber(int row, int col, int number);
}
