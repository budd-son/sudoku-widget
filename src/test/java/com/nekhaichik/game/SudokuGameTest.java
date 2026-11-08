package com.nekhaichik.game;

import com.nekhaichik.model.GameState;
import com.nekhaichik.model.Mode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuGameTest {
    private static final int[][] SOLUTION = {
            {1, 2, 3, 4, 5, 6, 7, 8, 9},
            {4, 5, 6, 7, 8, 9, 1, 2, 3},
            {7, 8, 9, 1, 2, 3, 4, 5, 6},
            {2, 3, 4, 5, 6, 7, 8, 9, 1},
            {5, 6, 7, 8, 9, 1, 2, 3, 4},
            {8, 9, 1, 2, 3, 4, 5, 6, 7},
            {3, 4, 5, 6, 7, 8, 9, 1, 2},
            {6, 7, 8, 9, 1, 2, 3, 4, 5},
            {9, 1, 2, 3, 4, 5, 6, 7, 8}
    };

    @Test
    void allowsEditingPlayerCellsAndDetectsCompletion() {
        int[][] puzzle = copy(SOLUTION);
        puzzle[0][0] = 0;
        SudokuGame game = SudokuGame.fromState(new GameState(
                puzzle, puzzle, Mode.EASY, 50, 70, 430, 540
        ));

        assertFalse(game.isSolved());
        game.setValue(0, 0, 1);
        assertTrue(game.isSolved());
    }

    @Test
    void doesNotAllowChangingGivenCells() {
        SudokuGame game = SudokuGame.fromState(new GameState(
                SOLUTION, SOLUTION, Mode.MEDIUM, 0, 0, 430, 540
        ));

        assertThrows(IllegalArgumentException.class, () -> game.setValue(0, 0, 2));
    }

    @Test
    void hintsFillAnEmptyCellWithoutChangingThePuzzle() {
        int[][] puzzle = copy(SOLUTION);
        puzzle[0][0] = 0;
        SudokuGame game = SudokuGame.fromState(new GameState(
                puzzle, puzzle, Mode.HARD, 0, 0, 430, 540
        ));

        assertTrue(game.addHint());
        assertEquals(1, game.values()[0][0]);
        assertEquals(0, game.puzzle()[0][0]);
    }

    private static int[][] copy(int[][] board) {
        int[][] copy = new int[board.length][];
        for (int row = 0; row < board.length; row++) {
            copy[row] = board[row].clone();
        }
        return copy;
    }
}
