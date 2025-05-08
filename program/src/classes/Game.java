package classes;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class Game {
    private final Path gameDir;
    private final Path stateFile;
    private final Path turnFile;
    private final GameManager manager;
    private List<Card> deck = new ArrayList<>();
    private List<User> players = new ArrayList<>();

    // Constructor to access game directory when it exists
    public Game(String name) throws IOException {
        gameDir = Paths.get(name);
        if (!Files.isDirectory(gameDir)) throw new IOException("Invalid game directory: " + name);
        manager = new GameManager(gameDir);
        turnFile = gameDir.resolve("turn.txt");
        stateFile = gameDir.resolve("state.txt");
    }

    /**
     * Initialize the game: create a deck of cards, shuffle it, and deal cards to players
     * @param name the name of the game
     */
    public static void initializeGame(String name) throws IOException {
        Path dir = Paths.get(name);
        if (Files.exists(dir)) throw new IOException("Game already exists: " + name);
        Files.createDirectory(dir);
        Files.createFile(dir.resolve("users.txt"));

        // Write initial state to state.txt file
        Files.createFile(dir.resolve("state.txt"));
        Files.write(dir.resolve("state.txt"), "INITIALIZING".getBytes());

        // Create a new game manager and initialize the game
        GameManager m = new GameManager(dir);
        m.initializeAdmin(System.console());
    }



}
