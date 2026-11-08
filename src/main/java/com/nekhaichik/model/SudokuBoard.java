package com.nekhaichik.model;

public class SudokuBoard {
    private final static int GRID_SIZE = 9;
    private Mode mode;
    private static int[][] BOARD = {
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

    public SudokuBoard() {
        this.mode = Mode.EASY;
    }
    public SudokuBoard(Mode mode) {
        this.mode = mode;
    }

    public static int[][] getBOARD() {
        return BOARD;
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
        int[][] board ={
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
        setBOARD(board);
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
}

