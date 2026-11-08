package com.nekhaichik.storage;

import com.nekhaichik.model.GameState;
import com.nekhaichik.model.Mode;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Properties;

public final class GameStateStore {
    private static final String VERSION = "1";
    private static final int GRID_SIZE = 9;

    private final Path file;

    public GameStateStore(Path file) {
        this.file = file.toAbsolutePath();
    }

    public Optional<GameState> load() throws IOException {
        if (!Files.exists(file)) {
            return Optional.empty();
        }

        Properties properties = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            properties.load(reader);
        }
        if (!VERSION.equals(properties.getProperty("version"))) {
            throw new IOException("Unsupported save-file version");
        }

        try {
            int[][] puzzle = parseBoard(required(properties, "puzzle"));
            int[][] values = parseBoard(required(properties, "values"));
            Mode mode = Mode.valueOf(required(properties, "mode"));
            return Optional.of(new GameState(
                    puzzle,
                    values,
                    mode,
                    Double.parseDouble(required(properties, "windowX")),
                    Double.parseDouble(required(properties, "windowY")),
                    Double.parseDouble(required(properties, "windowWidth")),
                    Double.parseDouble(required(properties, "windowHeight"))
            ));
        } catch (IllegalArgumentException e) {
            throw new IOException("Invalid save-file content", e);
        }
    }

    public void save(GameState state) throws IOException {
        Properties properties = new Properties();
        properties.setProperty("version", VERSION);
        properties.setProperty("mode", state.mode().name());
        properties.setProperty("puzzle", formatBoard(state.puzzle()));
        properties.setProperty("values", formatBoard(state.values()));
        properties.setProperty("windowX", Double.toString(state.windowX()));
        properties.setProperty("windowY", Double.toString(state.windowY()));
        properties.setProperty("windowWidth", Double.toString(state.windowWidth()));
        properties.setProperty("windowHeight", Double.toString(state.windowHeight()));

        Path parent = file.getParent();
        Files.createDirectories(parent);
        Path temporaryFile = Files.createTempFile(parent, "sudoku-", ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                properties.store(writer, "Sudoku widget saved game");
            }
            try {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private static String required(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing property: " + key);
        }
        return value;
    }

    private static String formatBoard(int[][] board) {
        StringBuilder result = new StringBuilder(GRID_SIZE * GRID_SIZE * 2);
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (result.length() > 0) {
                    result.append(',');
                }
                result.append(board[row][col]);
            }
        }
        return result.toString();
    }

    private static int[][] parseBoard(String serialized) {
        String[] cells = serialized.split(",", -1);
        if (cells.length != GRID_SIZE * GRID_SIZE) {
            throw new IllegalArgumentException("A board must contain 81 values");
        }
        int[][] board = new int[GRID_SIZE][GRID_SIZE];
        for (int index = 0; index < cells.length; index++) {
            board[index / GRID_SIZE][index % GRID_SIZE] = Integer.parseInt(cells[index]);
        }
        return board;
    }
}
