import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class Game {
    private static final String GAMES_FOLDER = "games";
    private final Path gameDir;
    private final Path turnFile;
    private final GameManager manager;

    /** Constructor: initializes the game directory if exists
     * * @param name the name of the game
     */
    public Game(String name) throws IOException {
        this.gameDir = Paths.get(GAMES_FOLDER, name);
        if (!Files.exists(gameDir)) throw new IllegalArgumentException("Game directory does not exist: " + name);
        this.turnFile = gameDir.resolve("turn.txt");
        this.manager = new GameManager(gameDir);
    }

    /** init: actually create a new game
     * * @param name the name of the game
     */
    public static void init(String name) throws IOException {
        Path gameDir = Paths.get(GAMES_FOLDER, name);
        if (Files.exists(gameDir)) throw new IllegalArgumentException("Game directory already exists: " + name);
        Files.createDirectories(gameDir);
        Files.createFile(gameDir.resolve("users.txt"));

        // Create the turn file with the initial turn
        String turn = "admin"; 
        Path turnFile = gameDir.resolve("turn.txt");
        Files.createFile(turnFile);
        Files.write(turnFile, turn.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        // Create the GameManager instance and initialize the game
        GameManager manager = new GameManager(gameDir);
        manager.initGame(System.console());
    }

    /** addUser: add a user to the game
     * * @param username the name of the user to add
     */
    public void addUser(String username) throws IOException {
        manager.requireUser("admin");
        if (!readTurn().equals("admin")) throw new IllegalStateException("Game already started: " + turnFile);
        manager.addUser(username);
    
    }

    /** removeUser: add a user to the game
     * * @param username the name of the user to add
     */
    public void removeUser(String username) throws IOException {
        manager.requireUser("admin");
        if (!readTurn().equals("admin")) throw new IllegalStateException("Game already started: " + turnFile);
        manager.removeUser(username);
        Files.deleteIfExists(gameDir.resolve(username + ".txt"));
    }

    /** readTurn: read the current turn from the turn file
     * * @return the current turn as a string
     */
    private String readTurn() throws IOException {
        if (!Files.exists(turnFile)) throw new IllegalAccessError("Turn file doesn't exist"); 
        return new String(Files.readAllBytes(turnFile), StandardCharsets.UTF_8);
    }

    /** writeTurn: write the current turn to the turn file
     * * @param turn the current turn as a string
     */
    private void writeTurn(String turn) throws IOException {
        if (!Files.exists(turnFile)) throw new IllegalAccessError("Turn file doesn't exist"); 
        Files.write(turnFile, turn.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}