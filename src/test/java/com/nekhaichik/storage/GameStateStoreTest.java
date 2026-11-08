package com.nekhaichik.storage;

import com.nekhaichik.model.GameState;
import com.nekhaichik.model.Mode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameStateStoreTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void roundTripsBoardDifficultyAndWindowBounds() throws IOException {
        int[][] puzzle = new int[9][9];
        puzzle[0][0] = 5;
        int[][] values = new int[9][9];
        values[0][0] = 5;
        values[2][4] = 7;
        GameState expected = new GameState(puzzle, values, Mode.HARD, 123, 234, 512, 612);
        GameStateStore store = new GameStateStore(temporaryDirectory.resolve("state/game.properties"));

        store.save(expected);
        Optional<GameState> loaded = store.load();

        assertEquals(Mode.HARD, loaded.orElseThrow().mode());
        assertBoardEquals(puzzle, loaded.orElseThrow().puzzle());
        assertBoardEquals(values, loaded.orElseThrow().values());
        assertEquals(123, loaded.orElseThrow().windowX());
        assertEquals(234, loaded.orElseThrow().windowY());
        assertEquals(512, loaded.orElseThrow().windowWidth());
        assertEquals(612, loaded.orElseThrow().windowHeight());
    }

    @Test
    void returnsEmptyWhenThereIsNoSavedGame() throws IOException {
        GameStateStore store = new GameStateStore(temporaryDirectory.resolve("missing.properties"));

        assertEquals(Optional.empty(), store.load());
    }

    @Test
    void reportsCorruptSaveFilesInsteadOfSilentlyIgnoringThem() throws IOException {
        Path file = temporaryDirectory.resolve("broken.properties");
        Files.writeString(file, "version=1\nmode=EASY\npuzzle=bad\n");

        assertThrows(IOException.class, () -> new GameStateStore(file).load());
    }

    private static void assertBoardEquals(int[][] expected, int[][] actual) {
        for (int row = 0; row < expected.length; row++) {
            assertEquals(expected[row].length, actual[row].length);
            for (int col = 0; col < expected[row].length; col++) {
                assertEquals(expected[row][col], actual[row][col]);
            }
        }
    }
}
