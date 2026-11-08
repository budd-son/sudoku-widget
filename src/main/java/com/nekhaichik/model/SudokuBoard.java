package com.nekhaichik.model;

public class SudokuBoard {
    private final static int GRID_SIZE = 9;
    private Mode mode;
    private static final int[][] DEFAULT_BOARD = {
            {1,2,3,4,5,6,7,8,9},
            {4,5,6,7,8,9,1,2,3},
            {7,8,9,1,2,3,4,5,6},
            {2,3,4,5,6,7,8,9,1},
            {5,6,7,8,9,1,2,3,4},
            {8,9,1,2,3,4,5,6,7},
            {3,4,5,6,7,8,9,1,2},
            {6,7,8,9,1,2,3,4,5},
            {9,1,2,3,4,5,6,7,8}
    };
    private static int[][] BOARD = copyOf(DEFAULT_BOARD);

    public SudokuBoard() {
        this.mode = Mode.EASY;
    }
    public SudokuBoard(Mode mode) {
        this.mode = mode;
    }

    public static int[][] getBOARD() {
        return BOARD;
    }

    public static int[][] createSolvedBoard() {
        return copyOf(DEFAULT_BOARD);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < GRID_SIZE; i++) {
            for(int j = 0; j < GRID_SIZE; j++) {
                sb.append(BOARD[i][j]).append( " ");
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    public static void reset(){
        BOARD = copyOf(DEFAULT_BOARD);
    }

    public static void setBOARD(int[][] BOARD) {
        SudokuBoard.BOARD = BOARD;
    }

    public Mode getMode() {
        return mode;
    }
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    private static int[][] copyOf(int[][] board) {
        int[][] copy = new int[GRID_SIZE][GRID_SIZE];
        for (int row = 0; row < GRID_SIZE; row++) {
            System.arraycopy(board[row], 0, copy[row], 0, GRID_SIZE);
        }
        return copy;
    }
}
