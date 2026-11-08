package com.nekhaichik.model;

import java.util.Objects;

public final class GameState {
    private static final int GRID_SIZE = 9;
    private static final double MIN_WINDOW_WIDTH = 300;
    private static final double MIN_WINDOW_HEIGHT = 400;

    private final int[][] puzzle;
    private final int[][] values;
    private final Mode mode;
    private final double windowX;
    private final double windowY;
    private final double windowWidth;
    private final double windowHeight;

    public GameState(
            int[][] puzzle,
            int[][] values,
            Mode mode,
            double windowX,
            double windowY,
            double windowWidth,
            double windowHeight
    ) {
        this.puzzle = copyAndValidate(puzzle, "puzzle");
        this.values = copyAndValidate(values, "values");
        this.mode = Objects.requireNonNull(mode, "mode");
        if (!Double.isFinite(windowX) || !Double.isFinite(windowY)
                || !Double.isFinite(windowWidth) || !Double.isFinite(windowHeight)
                || windowWidth < MIN_WINDOW_WIDTH || windowHeight < MIN_WINDOW_HEIGHT) {
            throw new IllegalArgumentException("Invalid window bounds");
        }
        this.windowX = windowX;
        this.windowY = windowY;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (this.puzzle[row][col] != 0 && this.values[row][col] != this.puzzle[row][col]) {
                    throw new IllegalArgumentException("Given puzzle values cannot be changed");
                }
            }
        }
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

    public double windowX() {
        return windowX;
    }

    public double windowY() {
        return windowY;
    }

    public double windowWidth() {
        return windowWidth;
    }

    public double windowHeight() {
        return windowHeight;
    }

    private static int[][] copyAndValidate(int[][] board, String name) {
        if (board == null || board.length != GRID_SIZE) {
            throw new IllegalArgumentException(name + " must have 9 rows");
        }
        int[][] copy = new int[GRID_SIZE][GRID_SIZE];
        for (int row = 0; row < GRID_SIZE; row++) {
            if (board[row] == null || board[row].length != GRID_SIZE) {
                throw new IllegalArgumentException(name + " rows must have 9 values");
            }
            for (int col = 0; col < GRID_SIZE; col++) {
                int value = board[row][col];
                if (value < 0 || value > 9) {
                    throw new IllegalArgumentException(name + " values must be between 0 and 9");
                }
                copy[row][col] = value;
            }
        }
        return copy;
    }

    private static int[][] copy(int[][] board) {
        int[][] copy = new int[GRID_SIZE][GRID_SIZE];
        for (int row = 0; row < GRID_SIZE; row++) {
            System.arraycopy(board[row], 0, copy[row], 0, GRID_SIZE);
        }
        return copy;
    }
}
