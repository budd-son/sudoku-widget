package com.nekhaichik.generator;
import com.nekhaichik.model.Mode;
import com.nekhaichik.solver.SudokuSolver;

import java.util.Random;

public class PrepareForSolve {
    private static final int GRID_SIZE = 9;
    public static void deleteCell(int[][] board, Mode mode) {
        int countForDelete = switch (mode) {
            case HARD -> 56;
            case MEDIUM -> 50;
            case EASY -> 45;
        };
        Random random = new Random();
        int attempts = 0;
        int maxAttempts = 100000;
        while (countForDelete > 0 && attempts < maxAttempts){
            int i = random.nextInt(GRID_SIZE);
            int j = random.nextInt(GRID_SIZE);
            if(!(board[i][j] == 0)) {
                int saved = board[i][j];
                board[i][j] = 0;

                if (SudokuSolver.solve(copyOf(board)) == 1) {
                    countForDelete--;
                } else {
                    board[i][j] = saved;
                }
            }
            attempts++;
        }
    }
    private static int[][] copyOf(int[][] board) {
        int[][] copy = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, GRID_SIZE);
        }
        return copy;
    }
}
