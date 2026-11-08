package com.nekhaichik.game;

import com.nekhaichik.generator.PrepareForSolve;
import com.nekhaichik.generator.SudokuGenerator;
import com.nekhaichik.model.GameState;
import com.nekhaichik.model.Mode;
import com.nekhaichik.model.SudokuBoard;
import com.nekhaichik.solver.SudokuSolver;

import java.util.Objects;

public final class SudokuGame {
    private static final int GRID_SIZE = 9;

    private final int[][] puzzle;
    private final int[][] values;
    private final Mode mode;

    private SudokuGame(int[][] puzzle, int[][] values, Mode mode) {
        this.puzzle = copy(puzzle);
        this.values = copy(values);
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    public static SudokuGame newGame(Mode mode) {
        Objects.requireNonNull(mode, "mode");
        int[][] puzzle = SudokuBoard.createSolvedBoard();
        SudokuGenerator.generateBoard(puzzle);
        PrepareForSolve.deleteCell(puzzle, mode);
        return new SudokuGame(puzzle, puzzle, mode);
    }

    public static SudokuGame fromState(GameState state) {
        Objects.requireNonNull(state, "state");
        return new SudokuGame(state.puzzle(), state.values(), state.mode());
    }

    public int[][] puzzle() {
        return copy(puzzle);
    }

    public int[][] values() {
        return copy(values);
    }

    public Mode mode() {
        return mode;
    }

    public boolean isGiven(int row, int col) {
        checkCell(row, col);
        return puzzle[row][col] != 0;
    }

    public void setValue(int row, int col, int value) {
        checkCell(row, col);
        if (value < 0 || value > 9) {
            throw new IllegalArgumentException("Cell values must be between 0 and 9");
        }
        if (puzzle[row][col] != 0 && value != puzzle[row][col]) {
            throw new IllegalArgumentException("Given puzzle values cannot be changed");
        }
        values[row][col] = value;
    }

    public boolean addHint() {
        int[][] solution = copy(values);
        if (!SudokuSolver.solveOne(solution)) {
            return false;
        }
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (values[row][col] == 0) {
                    values[row][col] = solution[row][col];
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSolved() {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                int value = values[row][col];
                if (value == 0) {
                    return false;
                }
                values[row][col] = 0;
                boolean valid = SudokuSolver.isValid(values, row, col, value);
                values[row][col] = value;
                if (!valid) {
                    return false;
                }
            }
        }
        return true;
    }

    public GameState toState(double x, double y, double width, double height) {
        return new GameState(puzzle, values, mode, x, y, width, height);
    }

    private static void checkCell(int row, int col) {
        if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) {
            throw new IndexOutOfBoundsException("Cell coordinates must be between 0 and 8");
        }
    }

    private static int[][] copy(int[][] board) {
        int[][] copy = new int[GRID_SIZE][GRID_SIZE];
        for (int row = 0; row < GRID_SIZE; row++) {
            System.arraycopy(board[row], 0, copy[row], 0, GRID_SIZE);
        }
        return copy;
    }
}
