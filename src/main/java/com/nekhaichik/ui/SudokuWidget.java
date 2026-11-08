package com.nekhaichik.ui;

import com.nekhaichik.generator.PrepareForSolve;
import com.nekhaichik.generator.SudokuGenerator;
import com.nekhaichik.model.Mode;
import com.nekhaichik.model.SudokuBoard;

import com.nekhaichik.solver.SudokuSolver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.*;
import java.util.Scanner;

public class SudokuWidget extends Application {
    private static final int GRID_SIZE = 9;
    private TextField[][] cells = new TextField[GRID_SIZE][GRID_SIZE];
    private int[][] currentBoard = new int[GRID_SIZE][GRID_SIZE];

    // Ссылки на элементы UI, чтобы бизнес-логика могла к ним обращаться
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.getStyleClass().add("widget-root");

        // Верхняя панель
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Судоку");
        title.getStyleClass().add("timer-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ComboBox<Mode> modeBox = new ComboBox<>();
        modeBox.getItems().addAll(Mode.EASY, Mode.MEDIUM, Mode.HARD);
        modeBox.setValue(Mode.EASY);

        topBar.getChildren().addAll(title, spacer, modeBox);

        // Сетка Судоку
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.getStyleClass().add("sudoku-grid");


        VBox.setVgrow(grid, Priority.ALWAYS);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setMaxHeight(Double.MAX_VALUE);

        // Создаем 9 колонок и 9 строк, каждая занимает ровно 1/9 пространства (11.11%)
        for (int i = 0; i < GRID_SIZE; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / GRID_SIZE);
            grid.getColumnConstraints().add(colConst);

            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / GRID_SIZE);
            grid.getRowConstraints().add(rowConst);
        }

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                TextField cell = new TextField();

                // ВАЖНО: привязываем размер шрифта к размеру клетки (умножаем на 0.5 для пропорции)
                cell.styleProperty().bind(
                        javafx.beans.binding.Bindings.concat(
                                "-fx-font-size: ", cell.widthProperty().multiply(0.5), "px;"
                        )
                );

                // Убираем жесткие размеры (setPrefSize), теперь они зависят от сетки
                cell.setAlignment(Pos.CENTER);
                cell.getStyleClass().add("sudoku-cell");

                cell.textProperty().addListener((obs, oldVal, newVal) -> {
                    if (!newVal.matches("[1-9]?")) {
                        cell.setText(oldVal);
                    }
                });

                if (col == 2 || col == 5) cell.getStyleClass().add("border-right");
                if (row == 2 || row == 5) cell.getStyleClass().add("border-bottom");

                cells[row][col] = cell;
                grid.add(cell, col, row);
            }
        }

        // Нижняя панель
        HBox bottomBar = new HBox(10);
        bottomBar.setAlignment(Pos.CENTER);

        Button generateBtn = new Button("Новая игра");
        Button solveBtn = new Button("Подсказка");

        statusLabel = new Label("Готов к игре");
        statusLabel.getStyleClass().add("status-label");

        generateBtn.setOnAction(e -> startNewGame(modeBox.getValue()));
        solveBtn.setOnAction(e -> makeHint());

        bottomBar.getChildren().addAll(generateBtn, solveBtn);

        root.getChildren().addAll(topBar, grid, bottomBar, statusLabel);

        // Задаем минимальный стартовый размер, дальше пользователь может растягивать
        Scene scene = new Scene(root, 350, 450);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/widget-style.css").toExternalForm());

        // Перетаскивание
        final double[] xOffset = {0};
        final double[] yOffset = {0};
        root.setOnMousePressed(event -> {
            xOffset[0] = event.getSceneX();
            yOffset[0] = event.getSceneY();
        });
        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - xOffset[0]);
            primaryStage.setY(event.getScreenY() - yOffset[0]);
        });

        // Устанавливаем минимальные размеры, чтобы сетка не ломалась при сильном сжатии
        primaryStage.setMinWidth(300);
        primaryStage.setMinHeight(400);

        primaryStage.setScene(scene);
        primaryStage.show();

        startNewGame(Mode.EASY);
    }
    private void startNewGame(Mode mode) {
        statusLabel.setText("Генерация...");

        new Thread(() -> {
            SudokuBoard.reset();
            SudokuGenerator.generateBoard(SudokuBoard.getBOARD());
            PrepareForSolve.deleteCell(SudokuBoard.getBOARD(), mode);

            int[][] newBoard = SudokuBoard.getBOARD();
            for (int i = 0; i < GRID_SIZE; i++) {
                System.arraycopy(newBoard[i], 0, currentBoard[i], 0, GRID_SIZE);
            }

            Platform.runLater(() -> {
                for (int row = 0; row < GRID_SIZE; row++) {
                    for (int col = 0; col < GRID_SIZE; col++) {
                        TextField cell = cells[row][col];
                        cell.getStyleClass().remove("fixed-cell");

                        if (currentBoard[row][col] != 0) {
                            cell.setText(String.valueOf(currentBoard[row][col]));
                            cell.setEditable(false);
                            cell.getStyleClass().add("fixed-cell");
                        } else {
                            cell.setText("");
                            cell.setEditable(true);
                        }
                    }
                }
                statusLabel.setText("Игра начата: " + mode);
            });
        }).start();
    }

    private void makeHint() {
        // 1. Ищем пустую клетку
        int emptyRow = -1, emptyCol = -1;
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (cells[i][j].getText().isEmpty()) { // Или currentBoard[i][j] == 0
                    emptyRow = i;
                    emptyCol = j;
                    break;
                }
            }
            if (emptyRow != -1) break;
        }

        if (emptyRow == -1) {
            statusLabel.setText("Доска уже заполнена!");
            return;
        }

        // 2. Делаем копию доски с учетом ввода игрока
        int[][] boardCopy = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                String text = cells[i][j].getText();
                boardCopy[i][j] = text.isEmpty() ? 0 : Integer.parseInt(text);
            }
        }

        // 3. Решаем копию
        if (SudokuSolver.solve(boardCopy) == 1) {
            int correctNum = boardCopy[emptyRow][emptyCol];

            // 5. Ставим в UI
            TextField cell = cells[emptyRow][emptyCol];
            cell.setText(String.valueOf(correctNum));
            cell.setEditable(false); // Блокируем, чтобы не меняли
            cell.getStyleClass().remove("sudoku-cell");
            cell.getStyleClass().add("fixed-cell"); // Красим как изначальную цифру

            statusLabel.setText("Подсказка добавлена!");
        } else {
            statusLabel.setText("Ошибка в ваших цифрах! Исправьте.");
        }
    }


    private boolean isValidMove(int[][] board, int num, int row, int col) {

        return SudokuSolver.isValid(board, num, row, col);
    }

    private void saveGame() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("sudoku_save.txt"))) {
            // Сохраняем сложность (опционально)
            // writer.println(modeBox.getValue());

            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    String text = cells[i][j].getText();
                    int val = text.isEmpty() ? 0 : Integer.parseInt(text);
                    writer.print(val + " ");
                }
                writer.println();
            }
            statusLabel.setText("Игра сохранена!");
        } catch (IOException e) {
            statusLabel.setText("Ошибка сохранения: " + e.getMessage());
        }
    }

    private void loadGame() {
        File file = new File("sudoku_save.txt");
        if (!file.exists()) {
            statusLabel.setText("Сохранений не найдено.");
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    if (scanner.hasNextInt()) {
                        int val = scanner.nextInt();
                        currentBoard[i][j] = val;

                        TextField cell = cells[i][j];
                        if (val != 0) {
                            cell.setText(String.valueOf(val));
                            // Логика блокировки: если цифра была изначально, её не изменить
                            // (В идеале надо сохранять и маску изначальных цифр, но для простоты заблокируем все)
                            cell.setEditable(false);
                            cell.getStyleClass().add("fixed-cell");
                        } else {
                            cell.setText("");
                            cell.setEditable(true);
                            cell.getStyleClass().remove("fixed-cell");
                        }
                    }
                }
            }
            statusLabel.setText("Игра загружена!");
        } catch (FileNotFoundException e) {
            statusLabel.setText("Ошибка загрузки.");
        }
    }

    private boolean checkWin() {
        // Считываем текущую доску из UI
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                String text = cells[i][j].getText();
                if (text.isEmpty()) return false; // Есть пустые клетки — не победа

                int num = Integer.parseInt(text);
                if (!isValidMove(currentBoard, num, i, j)) {
                    return false; // Есть конфликты — не победа
                }
            }
        }
        return true; // Всё заполнено и без ошибок!
    }

    public static void main(String[] args) {
        launch(args);
    }
}