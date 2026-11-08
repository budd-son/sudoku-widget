package com.nekhaichik.generator;

import com.nekhaichik.model.SudokuBoard;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuGeneratorTest {
    @Test
    void randomizesTheProvidedBoardWithoutChangingTheGlobalBoard() {
        SudokuBoard.reset();
        int[][] globalBoard = copy(SudokuBoard.getBOARD());
        int[][] generatedBoard = copy(globalBoard);

        for (int attempt = 0; attempt < 20; attempt++) {
            generatedBoard = copy(globalBoard);
            SudokuGenerator.generateBoard(generatedBoard);
            assertValidSolution(generatedBoard);
            assertBoardEquals(globalBoard, SudokuBoard.getBOARD());
        }
    }

    private static void assertValidSolution(int[][] board) {
        for (int row = 0; row < 9; row++) {
            boolean[] rowValues = new boolean[10];
            boolean[] columnValues = new boolean[10];
            for (int col = 0; col < 9; col++) {
                rowValues[board[row][col]] = true;
                columnValues[board[col][row]] = true;
            }
            for (int value = 1; value <= 9; value++) {
                assertTrue(rowValues[value]);
                assertTrue(columnValues[value]);
            }
        }
        for (int boxRow = 0; boxRow < 9; boxRow += 3) {
            for (int boxCol = 0; boxCol < 9; boxCol += 3) {
                boolean[] boxValues = new boolean[10];
                for (int row = boxRow; row < boxRow + 3; row++) {
                    for (int col = boxCol; col < boxCol + 3; col++) {
                        boxValues[board[row][col]] = true;
                    }
                }
                for (int value = 1; value <= 9; value++) {
                    assertTrue(boxValues[value]);
                }
            }
        }
    }

    private static void assertBoardEquals(int[][] expected, int[][] actual) {
        for (int row = 0; row < expected.length; row++) {
            for (int col = 0; col < expected[row].length; col++) {
                assertEquals(expected[row][col], actual[row][col]);
            }
        }
    }

    private static int[][] copy(int[][] board) {
        int[][] copy = new int[board.length][];
        for (int row = 0; row < board.length; row++) {
            copy[row] = board[row].clone();
        }
        return copy;
    }
}
