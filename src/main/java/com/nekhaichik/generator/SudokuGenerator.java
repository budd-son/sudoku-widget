package com.nekhaichik.generator;

import java.util.Random;
public class SudokuGenerator {
    private final static int GRID_SIZE = 9;
    private final static int BOX_SIZE = 3;
    public static void generateBoard(int[][] board) {
        Random random = new Random();
        int iterations = random.nextInt(2, 21);
        for (int i = 0; i < iterations; i++) {
            int method = random.nextInt(4) + 1;
            switch (method) {
            case 1:
                transposeBoard(board);
                break;
            case 2:
                int row1 = random.nextInt(GRID_SIZE);
                int row2 = row1 - row1%3 + random.nextInt(3);
                swapRow(board, row1, row2);
                break;
            case 3:
                int col1 = random.nextInt(GRID_SIZE);
                int col2 = col1 - col1%3 + random.nextInt(3);
                swapColumn(board, col1, col2);
                break;
            case 4:
                int firstBand = random.nextInt(3);
                int secondBand = (firstBand + random.nextInt(1, 3)) % 3;
                swapRowBands(board, firstBand, secondBand);
                break;
            }
        }
    }

    private static void transposeBoard(int[][] board) {
        int k;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = i+1; j < GRID_SIZE; j++) {
                k = board[i][j];
                board[i][j] = board[j][i];
                board[j][i] = k;
            }
        }
    }
    private static void swapRow(int[][] board, int row1, int row2) {
        int[] rememberRow1 = board[row1];
        board[row1] = board[row2];
        board[row2] = rememberRow1;
    }
    private static void swapColumn(int[][] board, int col1, int col2) {
        for (int i = 0; i < GRID_SIZE; i++) {
            int temp = board[i][col1];
            board[i][col1] = board[i][col2];
            board[i][col2] = temp;
        }
    }
    private static void swapRowBands(int[][] board, int firstBand, int secondBand) {
        for (int row = 0; row < BOX_SIZE; row++) {
            swapRow(board, firstBand * BOX_SIZE + row, secondBand * BOX_SIZE + row);
        }
    }
}
