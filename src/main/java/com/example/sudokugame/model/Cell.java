package com.example.sudokugame.model;

/**
 * Representa una celda individual del tablero de Sudoku 6x6.
 * Cada celda almacena un valor numerico entre 1 y 6 (o 0 si esta vacia)
 * y mantiene informacion sobre si el valor fue colocado inicialmente
 * o si fue introducido por el usuario.
 */
public class Cell {

    /** Valor actual de la celda. 0 indica celda vacia. */
    private int value;

    /** Indica si la celda es un valor fijo del tablero inicial. */
    private final boolean fixed;

    /**
     * Construye una celda con un valor y un estado de fijeza.
     *
     * @param value valor inicial (0 si esta vacia).
     * @param fixed true si la celda pertenece al tablero inicial.
     */
    public Cell(int value, boolean fixed) {
        this.value = value;
        this.fixed = fixed;
    }

    /**
     * Obtiene el valor actual de la celda.
     *
     * @return el valor almacenado (0 si esta vacia).
     */
    public int getValue() {
        return value;
    }

    /**
     * Establece un nuevo valor para la celda.
     *
     * @param value nuevo valor (0 a 6).
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Indica si la celda es un valor fijo del tablero inicial.
     *
     * @return true si el usuario no puede modificar esta celda.
     */
    public boolean isFixed() {
        return fixed;
    }

    /**
     * Indica si la celda esta vacia.
     *
     * @return true si el valor es 0.
     */
    public boolean isEmpty() {
        return value == 0;
    }
}
