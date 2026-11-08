package com.nekhaichik.ui;

import com.nekhaichik.game.SudokuGame;
import com.nekhaichik.model.GameState;
import com.nekhaichik.model.Mode;
import com.nekhaichik.storage.GameStateStore;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class SudokuWidget extends Application {
    private static final int GRID_SIZE = 9;
    private static final double INITIAL_WIDTH = 430;
    private static final double INITIAL_HEIGHT = 540;
    private static final double RESIZE_BORDER = 10;

    private final TextField[][] cells = new TextField[GRID_SIZE][GRID_SIZE];
    private final GameStateStore stateStore = new GameStateStore(
            Path.of(System.getProperty("user.home"), ".sudoku-widget", "game.properties")
    );
    private final PauseTransition saveDelay = new PauseTransition(Duration.millis(350));

    private SudokuGame game;
    private Stage stage;
    private Label statusLabel;
    private ComboBox<Mode> modeBox;
    private Button generateButton;
    private boolean applyingState;
    private ResizeEdges resizing = ResizeEdges.NONE;
    private double pressScreenX;
    private double pressScreenY;
    private double pressWindowX;
    private double pressWindowY;
    private double pressWindowWidth;
    private double pressWindowHeight;

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setResizable(true);
        stage.setMinWidth(300);
        stage.setMinHeight(400);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        root.getStyleClass().add("widget-root");

        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Судоку");
        title.getStyleClass().add("timer-label");
        title.setCursor(Cursor.MOVE);
        title.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                pressWindowX = event.getSceneX();
                pressWindowY = event.getSceneY();
            }
        });
        title.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                stage.setX(event.getScreenX() - pressWindowX);
                stage.setY(event.getScreenY() - pressWindowY);
                scheduleSave();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        modeBox = new ComboBox<>();
        modeBox.getItems().addAll(Mode.EASY, Mode.MEDIUM, Mode.HARD);
        modeBox.setValue(Mode.EASY);
        topBar.getChildren().addAll(title, spacer, modeBox);

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.getStyleClass().add("sudoku-grid");
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(grid, Priority.ALWAYS);

        for (int i = 0; i < GRID_SIZE; i++) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(100.0 / GRID_SIZE);
            grid.getColumnConstraints().add(column);
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / GRID_SIZE);
            grid.getRowConstraints().add(row);
        }

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                TextField cell = new TextField();
                cell.styleProperty().bind(
                        javafx.beans.binding.Bindings.concat(
                                "-fx-font-size: ", cell.widthProperty().multiply(0.5), "px;"
                        )
                );
                cell.setAlignment(Pos.CENTER);
                cell.getStyleClass().add("sudoku-cell");
                int cellRow = row;
                int cellCol = col;
                cell.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.matches("[1-9]?")) {
                        cell.setText(oldValue);
                        return;
                    }
                    if (!applyingState && game != null) {
                        game.setValue(cellRow, cellCol, newValue.isEmpty() ? 0 : Integer.parseInt(newValue));
                        scheduleSave();
                        if (game.isSolved()) {
                            statusLabel.setText("Победа! Судоку решено.");
                        }
                    }
                });
                if (col == 2 || col == 5) {
                    cell.getStyleClass().add("border-right");
                }
                if (row == 2 || row == 5) {
                    cell.getStyleClass().add("border-bottom");
                }
                cells[row][col] = cell;
                grid.add(cell, col, row);
            }
        }

        HBox bottomBar = new HBox(6);
        bottomBar.setAlignment(Pos.CENTER);
        generateButton = new Button("Новая игра");
        Button hintButton = new Button("Подсказка");
        Button saveButton = new Button("Сохранить");
        Button loadButton = new Button("Загрузить");
        statusLabel = new Label("Готов к игре");
        statusLabel.getStyleClass().add("status-label");

        generateButton.setOnAction(event -> startNewGame(modeBox.getValue()));
        hintButton.setOnAction(event -> makeHint());
        saveButton.setOnAction(event -> persistState(true));
        loadButton.setOnAction(event -> loadGame());
        bottomBar.getChildren().addAll(generateButton, hintButton, saveButton, loadButton);
        root.getChildren().addAll(topBar, grid, bottomBar, statusLabel);
        installResizeHandlers(root);

        Scene scene = new Scene(root, INITIAL_WIDTH, INITIAL_HEIGHT);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/widget-style.css").toExternalForm());
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> persistState(false));
        saveDelay.setOnFinished(event -> persistState(false));

        Optional<GameState> savedState;
        try {
            savedState = stateStore.load();
        } catch (IOException e) {
            savedState = Optional.empty();
            statusLabel.setText("Ошибка загрузки сохранения: " + e.getMessage());
        }
        if (savedState.isPresent()) {
            restoreState(savedState.get());
            statusLabel.setText("Сохранённая игра загружена.");
        }
        stage.show();
        if (savedState.isEmpty()) {
            startNewGame(Mode.EASY);
        } else {
            scheduleSave();
        }
    }

    private void startNewGame(Mode mode) {
        generateButton.setDisable(true);
        statusLabel.setText("Генерация новой игры...");
        Task<SudokuGame> task = new Task<>() {
            @Override
            protected SudokuGame call() {
                return SudokuGame.newGame(mode);
            }
        };
        task.setOnSucceeded(event -> {
            game = task.getValue();
            modeBox.setValue(mode);
            showGame();
            statusLabel.setText("Игра начата: " + mode);
            generateButton.setDisable(false);
            scheduleSave();
        });
        task.setOnFailed(event -> {
            statusLabel.setText("Ошибка генерации: " + task.getException().getMessage());
            generateButton.setDisable(false);
        });
        Thread worker = new Thread(task, "sudoku-generator");
        worker.setDaemon(true);
        worker.start();
    }

    private void makeHint() {
        if (game == null) {
            return;
        }
        if (!game.addHint()) {
            statusLabel.setText("Подсказка недоступна: проверьте введённые цифры.");
            return;
        }
        showGame();
        statusLabel.setText("Подсказка добавлена.");
        scheduleSave();
    }

    private void loadGame() {
        try {
            Optional<GameState> savedState = stateStore.load();
            if (savedState.isEmpty()) {
                statusLabel.setText("Сохранённая игра не найдена.");
                return;
            }
            restoreState(savedState.get());
            statusLabel.setText("Сохранённая игра загружена.");
        } catch (IOException e) {
            statusLabel.setText("Ошибка загрузки сохранения: " + e.getMessage());
        }
    }

    private void restoreState(GameState state) {
        game = SudokuGame.fromState(state);
        modeBox.setValue(game.mode());
        applyingState = true;
        try {
            showGame();
        } finally {
            applyingState = false;
        }
        stage.setX(state.windowX());
        stage.setY(state.windowY());
        stage.setWidth(state.windowWidth());
        stage.setHeight(state.windowHeight());
    }

    private void showGame() {
        int[][] puzzle = game.puzzle();
        int[][] values = game.values();
        applyingState = true;
        try {
            for (int row = 0; row < GRID_SIZE; row++) {
                for (int col = 0; col < GRID_SIZE; col++) {
                    TextField cell = cells[row][col];
                    cell.setText(values[row][col] == 0 ? "" : Integer.toString(values[row][col]));
                    cell.setEditable(puzzle[row][col] == 0);
                    cell.getStyleClass().remove("fixed-cell");
                    if (puzzle[row][col] != 0) {
                        cell.getStyleClass().add("fixed-cell");
                    }
                }
            }
        } finally {
            applyingState = false;
        }
    }

    private void scheduleSave() {
        if (stage != null && stage.isShowing() && game != null) {
            saveDelay.playFromStart();
        }
    }

    private void persistState(boolean showSuccess) {
        if (stage == null || !stage.isShowing() || game == null) {
            return;
        }
        try {
            stateStore.save(game.toState(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight()));
            if (showSuccess) {
                statusLabel.setText("Игра сохранена.");
            }
        } catch (IOException | IllegalArgumentException e) {
            String message = "Ошибка сохранения: " + e.getMessage();
            statusLabel.setText(message);
            System.err.println(message);
        }
    }

    private void installResizeHandlers(Node root) {
        root.setOnMouseMoved(event -> {
            if (resizing == ResizeEdges.NONE) {
                root.setCursor(edgesAt(event.getX(), event.getY(), root).cursor());
            }
        });
        root.setOnMouseExited(event -> {
            if (resizing == ResizeEdges.NONE) {
                root.setCursor(Cursor.DEFAULT);
            }
        });
        root.setOnMousePressed(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            resizing = edgesAt(event.getX(), event.getY(), root);
            if (resizing != ResizeEdges.NONE) {
                pressScreenX = event.getScreenX();
                pressScreenY = event.getScreenY();
                pressWindowX = stage.getX();
                pressWindowY = stage.getY();
                pressWindowWidth = stage.getWidth();
                pressWindowHeight = stage.getHeight();
                event.consume();
            }
        });
        root.setOnMouseDragged(event -> {
            if (resizing == ResizeEdges.NONE) {
                return;
            }
            double deltaX = event.getScreenX() - pressScreenX;
            double deltaY = event.getScreenY() - pressScreenY;
            double width = pressWindowWidth + (resizing.east ? deltaX : resizing.west ? -deltaX : 0);
            double height = pressWindowHeight + (resizing.south ? deltaY : resizing.north ? -deltaY : 0);
            width = Math.max(stage.getMinWidth(), width);
            height = Math.max(stage.getMinHeight(), height);
            stage.setWidth(width);
            stage.setHeight(height);
            if (resizing.west) {
                stage.setX(pressWindowX + pressWindowWidth - width);
            }
            if (resizing.north) {
                stage.setY(pressWindowY + pressWindowHeight - height);
            }
            event.consume();
        });
        root.setOnMouseReleased(event -> {
            if (resizing != ResizeEdges.NONE) {
                resizing = ResizeEdges.NONE;
                root.setCursor(Cursor.DEFAULT);
                scheduleSave();
                event.consume();
            }
        });
    }

    private static ResizeEdges edgesAt(double x, double y, Node root) {
        double width = root.getBoundsInLocal().getWidth();
        double height = root.getBoundsInLocal().getHeight();
        boolean west = x <= RESIZE_BORDER;
        boolean east = x >= width - RESIZE_BORDER;
        boolean north = y <= RESIZE_BORDER;
        boolean south = y >= height - RESIZE_BORDER;
        return new ResizeEdges(north, south, east, west);
    }

    private record ResizeEdges(boolean north, boolean south, boolean east, boolean west) {
        private static final ResizeEdges NONE = new ResizeEdges(false, false, false, false);

        private Cursor cursor() {
            if ((north && west) || (south && east)) {
                return Cursor.NW_RESIZE;
            }
            if ((north && east) || (south && west)) {
                return Cursor.NE_RESIZE;
            }
            if (north || south) {
                return Cursor.V_RESIZE;
            }
            if (east || west) {
                return Cursor.H_RESIZE;
            }
            return Cursor.DEFAULT;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
