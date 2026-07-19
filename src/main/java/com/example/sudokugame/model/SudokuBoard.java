package com.example.sudokugame;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Modelo principal del Sudoku 6x6.
 * Contiene la cuadricula, las reglas de validacion, el generador
 * de tableros validos y el resolvedor (backtracking) necesario
 * para la funcion de ayuda.
 *
 * El tablero se divide en bloques de 2 filas por 3 columnas,
 * cada bloque debe contener los numeros del 1 al 6 sin repetir.
 */
public class SudokuBoard {

    /** Tamanio del lado del tablero. */
    public static final int SIZE = 6;

    /** Numero de filas por bloque. */
    public static final int BOX_ROWS = 2;

    /** Numero de columnas por bloque. */
    public static final int BOX_COLS = 3;

    /** Numero de celdas fijas a colocar al iniciar. */
    public static final int INITIAL_HINTS = 8;

    /** Cuadricula de celdas. */
    private final Cell[][] grid;

    /** Solucion completa del tablero, usada para validar y ayudar. */
    private int[][] solution;

    /** Generador de numeros aleatorios. */
    private final Random random;

    /**
     * Construye un tablero vacio. Se debe llamar a {@link #newGame()}
     * para inicializar un nuevo juego.
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
     * Genera un nuevo juego: primero construye una solucion valida
     * y luego coloca exactamente 2 pistas en posiciones aleatorias,
     * verificando que el tablero siga siendo resoluble.
     */
    public void newGame() {
        int attempts = 0;
        while (attempts < 50) {
            generateFullSolution();
            if (tryPlaceHints(INITIAL_HINTS)) {
                return;
            }
            attempts++;
        }
        generateFullSolution();
        forcePlaceHints(INITIAL_HINTS);
    }

    /**
     * Intenta colocar la cantidad indicada de pistas verificando que
     * el tablero resultante tenga solucion unica.
     *
     * @param count numero de pistas deseadas.
     * @return true si se pudieron colocar todas.
     */
    private boolean tryPlaceHints(int count) {
        clearBoard();
        List<int[]> positions = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                positions.add(new int[]{r, c});
            }
        }
        Collections.shuffle(positions, random);
        int placed = 0;
        for (int[] pos : positions) {
            if (placed >= count) {
                return true;
            }
            int r = pos[0];
            int c = pos[1];
            grid[r][c] = new Cell(solution[r][c], true);
            if (!hasUniqueSolution()) {
                grid[r][c] = new Cell(0, false);
            } else {
                placed++;
            }
        }
        // No se alcanzaron a colocar suficientes pistas, limpia el tablero.
        clearBoard();
        return false;
    }

    /**
     * Coloca pistas sin verificar unicidad. Se usa como red de seguridad.
     *
     * @param count numero de pistas a colocar.
     */
    private void forcePlaceHints(int count) {
        clearBoard();
        List<int[]> positions = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                positions.add(new int[]{r, c});
            }
        }
        Collections.shuffle(positions, random);
        for (int i = 0; i < count && i < positions.size(); i++) {
            int[] pos = positions.get(i);
            grid[pos[0]][pos[1]] = new Cell(solution[pos[0]][pos[1]], true);
        }
    }

    /**
     * Limpia el tablero dejando todas las celdas vacias y no fijas.
     */
    private void clearBoard() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = new Cell(0, false);
            }
        }
    }

    /**
     * Verifica que el tablero actual (con celdas fijas) tenga
     * una unica solucion. Se usa antes de mostrar el juego.
     *
     * @return true si existe una sola solucion.
     */
    private boolean hasUniqueSolution() {
        int[][] copy = snapshotNonFixed();
        int[] count = new int[]{0};
        solveUnique(copy, count, 2);
        return count[0] == 1;
    }

    /**
     * Construye una copia del tablero donde las celdas no fijas se
     * representan con 0. Se usa para resolver y verificar unicidad.
     *
     * @return copia del tablero.
     */
    private int[][] snapshotNonFixed() {
        int[][] copy = new int[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                copy[r][c] = grid[r][c].getValue();
            }
        }
        return copy;
    }

    /**
     * Busca hasta {@code limit} soluciones en el tablero recibido.
     * Implementa backtracking clasico.
     *
     * @param board tablero a resolver (0 representa vacio).
     * @param count arreglo de un entero usado como contador mutable.
     * @param limit numero maximo de soluciones a buscar.
     * @return true si se alcanzo el limite de soluciones.
     */
    private boolean solveUnique(int[][] board, int[] count, int limit) {
        if (count[0] >= limit) {
            return true;
        }
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] == 0) {
                    for (int n = 1; n <= SIZE; n++) {
                        if (isValidPlacement(board, r, c, n)) {
                            board[r][c] = n;
                            if (solveUnique(board, count, limit)) {
                                return true;
                            }
                            board[r][c] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        count[0]++;
        return count[0] >= limit;
    }

    /**
     * Genera una solucion completa valida para un tablero 6x6
     * con bloques 2x3.
     */
    private void generateFullSolution() {
        solution = new int[SIZE][SIZE];
        fillSolution(0, 0);
    }

    /**
     * Rellena recursivamente la matriz {@link #solution} por filas
     * usando backtracking con orden aleatorio para variar las soluciones.
     *
     * @param row fila actual.
     * @param col columna actual.
     * @return true si logro completar la solucion.
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
     * Verifica si un numero puede colocarse en una posicion respetando
     * las reglas de fila, columna y bloque 2x3.
     *
     * @param board tablero a evaluar.
     * @param row fila destino.
     * @param col columna destino.
     * @param number numero a colocar.
     * @return true si la colocacion es valida.
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
     * Devuelve la celda en la posicion indicada.
     *
     * @param row fila.
     * @param col columna.
     * @return la celda correspondiente.
     */
    public Cell getCell(int row, int col) {
        return grid[row][col];
    }

    /**
     * Obtiene el valor almacenado en una celda.
     *
     * @param row fila.
     * @param col columna.
     * @return valor actual (0 si esta vacia).
     */
    public int getValue(int row, int col) {
        return grid[row][col].getValue();
    }

    /**
     * Obtiene el valor correcto (segun la solucion) para una celda.
     *
     * @param row fila.
     * @param col columna.
     * @return valor correcto.
     */
    public int getSolutionValue(int row, int col) {
        return solution[row][col];
    }

    /**
     * Coloca un valor en una celda modificable, solo si no esta repetido
     * en su fila, columna o bloque 2x3 respecto a las demas celdas.
     *
     * @param row fila.
     * @param col columna.
     * @param number numero a colocar.
     * @return true si se coloco correctamente.
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
     * Indica si un numero ya esta siendo usado por otra celda en la
     * misma fila, columna o bloque 2x3. No considera la propia celda.
     *
     * @param row fila de la celda destino.
     * @param col columna de la celda destino.
     * @param number numero a verificar.
     * @return true si el numero ya esta ocupado.
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
     * Borra el contenido de una celda modificable.
     *
     * @param row fila.
     * @param col columna.
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
     * Indica si el valor actual de la celda coincide con la solucion.
     *
     * @param row fila.
     * @param col columna.
     * @return true si coincide, false en caso contrario o si esta vacia.
     */
    public boolean isCorrect(int row, int col) {
        if (grid[row][col].isEmpty()) {
            return false;
        }
        return grid[row][col].getValue() == solution[row][col];
    }

    /**
     * Indica si el tablero esta completamente lleno y correcto.
     *
     * @return true si el jugador ha ganado.
     */
    public boolean isSolved() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c].isEmpty() || grid[r][c].getValue() != solution[r][c]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Cuenta cuantas celdas vacias quedan. Utilizado para saber si la
     * ayuda debe desactivarse cuando solo queda una por llenar.
     *
     * @return numero de celdas vacias.
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
     * Busca una celda vacia que pueda rellenarse con un valor que
     * mantenga el tablero resoluble (que conduzca a la victoria).
     * El valor colocado debe coincidir con la solucion para que el
     * tablero pueda completarse.
     *
     * @return coordenadas de la celda a rellenar o null si no hay
     *         celdas candidatas.
     */
    public int[] pickHelpCell() {
        List<int[]> candidates = new ArrayList<>();
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c].isEmpty()) {
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
     * Rellena la celda indicada con su valor correcto de la solucion.
     *
     * @param row fila.
     * @param col columna.
     */
    public void applyHelp(int row, int col) {
        if (grid[row][col].isFixed() || !grid[row][col].isEmpty()) {
            return;
        }
        grid[row][col].setValue(solution[row][col]);
    }

    /**
     * Verifica que la colocacion actual respete las reglas del sudoku.
     * Se usa para detectar y resaltar errores.
     *
     * @param row fila.
     * @param col columna.
     * @return true si el valor actual viola alguna regla.
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
     * Devuelve una representacion en texto del tablero, util para
     * depuracion.
     *
     * @return cadena con el contenido del tablero.
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
