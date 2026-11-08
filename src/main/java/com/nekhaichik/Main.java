package com.nekhaichik;

import com.nekhaichik.generator.PrepareForSolve;
import com.nekhaichik.generator.SudokuGenerator;
import com.nekhaichik.model.SudokuBoard;

public class Main {
    public static void main(String[] args) {
        SudokuBoard board = new SudokuBoard();
        SudokuGenerator.generateBoard(SudokuBoard.getBOARD());
        System.out.print(board);
        PrepareForSolve.deleteCell(SudokuBoard.getBOARD(), board.getMode());
        System.out.println();
        System.out.print(board);
    }
}