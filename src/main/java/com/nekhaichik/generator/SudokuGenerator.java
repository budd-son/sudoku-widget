package com.nekhaichik.generator;

import com.nekhaichik.model.SudokuBoard;
import java.util.Random;
public class SudokuGenerator {
    private final static int GRID_SIZE = 9;
    private final static int BOX_SIZE = 3;
    public static void generateBoard(int[][] board) {
        Random random = new Random();
        int[][] generatedBoard = SudokuBoard.getBOARD();
        int iterations = random.nextInt(2, 21);
        for (int i = 0; i < iterations; i++) {
            int method = random.nextInt(4) + 1;
            //System.out.println(method);
            switch (method) {
            case 1:
                transposeBoard(generatedBoard);
                break;
            case 2:
                int row1 = random.nextInt(GRID_SIZE);
                int row2 = row1 - row1%3 + random.nextInt(3);
                swapRow(generatedBoard, row1, row2);
                break;
            case 3:
                int col1 = random.nextInt(GRID_SIZE);
                int col2 = col1 - col1%3 + random.nextInt(3);
                swapColumn(generatedBoard,  col1, col2);
                break;
            case 4:
                int box1 = random.nextInt(GRID_SIZE);
                int row = box1 / 3;
                int col = box1 % 3;

                int[] buf = new int[4];
                int count = 0;
                if (col > 0) buf[count++] = box1 - 1;
                if (col < 2) buf[count++] = box1 + 1;
                if (row > 0) buf[count++] = box1 - 3;
                if (row < 2) buf[count++] = box1 + 3;

                int box2 = buf[random.nextInt(count)];

                swapBox(generatedBoard, box1, box2);
                break;
            }
        }
        SudokuBoard.setBOARD(generatedBoard);
        /*
        for(int i = 0; i < GRID_SIZE; i++) {
            for(int j = 0; j < GRID_SIZE; j++) {
                System.out.print(generatedBoard[i][j] + " ");
            }
            System.out.println();
        }
         */
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
    private static void swapBox(int[][] board, int box1, int box2) {
        int[][] rememberBox1 = new int[BOX_SIZE][BOX_SIZE];
        int row1 = (box1 / 3) * 3;
        int col1 = (box1 % 3) * 3;
        int row2 = (box2 / 3) * 3;
        int col2 = (box2 % 3) * 3;
        for (int i = 0; i < BOX_SIZE; i++) {
            for (int j = 0; j < BOX_SIZE; j++) {
                int temp = board[row1 + i][col1 + j];
                board[row1 + i][col1 + j] = board[row2 + i][col2 + j];
                board[row2 + i][col2 + j] = temp;
            }
        }
    }
}
