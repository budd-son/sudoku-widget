package com.nekhaichik.solver;

public class SudokuSolver {
    private final static int GRID_SIZE = 9;
    public static int solve(int[][] board) {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (board[i][j] == 0) {
                    int count = 0;
                    for (int k = 1; k <= 9; k++) {
                        if (isValid(board, i, j, k)) {
                            board[i][j] = k;
                            count += solve(board);
                            board[i][j] = 0;
                            if (count >= 2) {
                                return count;
                            }
                        }
                    }
                    return count;
                }
            }
        }
        return 1;
    }

    public static boolean isValid(int[][] board, int row, int col, int value) {
        return !isNumberInBox(board, value, row, col)&&!isNumberInColumn(board,value,col)&&!isNumberInRow(board,value,row);
    }

    public static boolean isNumberInRow(int[][] board, int number, int row) {
        for (int i = 0; i < GRID_SIZE; i++) {
            if (board[row][i] == number) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNumberInColumn(int[][] board, int number, int column) {
        for (int i = 0; i < GRID_SIZE; i++) {
            if (board[i][column] == number) {
                    return true;
            }
        }
        return false;
    }

    public static boolean isNumberInBox(int[][] board, int number, int row, int column) {
        int localRow = row - row%3;
        int localColumn = column - column%3 ;
        for (int i = localRow; i < localRow + 3; i++) {
            for (int j = localColumn; j < localColumn + 3; j++) {
                if (board[i][j] == number) {
                    return true;
                }
            }
        }
        return false;
    }

}
