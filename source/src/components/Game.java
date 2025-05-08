package components;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class Game {
    private final Path gameDir;
    private final List<User> users = new ArrayList<>(); // List of users in the game
    private final GameFileManager manager; // File manager for game data
    private final Path turnFile; // File to save the game turn and state of the game

    public Game(String name) throws IOException {
        this.gameDir = Paths.get(name); // Directory for the game
        if (!Files.exists(gameDir)) throw new IllegalArgumentException("Game directory does not exist: " + name);
        manager = new GameFileManager(gameDir); // Initialize file manager
        turnFile = gameDir.resolve("turn.txt"); // File to save the game turn
    }

    public static void init(String gameName) throws IOException {
        Path gameDir = Paths.get(gameName); // Directory for the game
        if (Files.exists(gameDir)) throw new IllegalArgumentException("Game directory already exists: " + gameName);
        Files.createDirectories(gameDir); // Create the game directory
        Files.createFile(gameDir.resolve("users.txt")); // Create the users file
        Files.createFile(gameDir.resolve("turn.txt")); // Create the turn file
        GameFileManager manager = new GameFileManager(gameDir); // Initialize file manager
        manager.initializeAdmin(System.console()); // Add admin user
    }

    private String readTurnFile() throws IOException {
        if (!Files.exists(turnFile)) throw new IllegalAccessError("Turn file doesn't exist"); 
        String s = new String(Files.readAllBytes(turnFile), StandardCharsets.UTF_8); // Read the file content
        s.trim(); // Trim whitespace
        return s; // Return the content
    }

    private void writeTurnFile(String s) throws IOException {
        if (!Files.exists(turnFile)) throw new IllegalAccessError("Turn file doesn't exist"); 
        Files.write(turnFile, s.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING); // Write to the file
    }

    public void addUser(String u) throws IOException {
        manager.requireUser(System.console(), "admin");
        if (readTurnFile() != "admin") throw new IllegalArgumentException("Game is already in progress. Cannot add user.");
        manager.addUser(System.console(), u); // Add user to the game
    }

    public void removeUser(String u) throws IOException {
        manager.requireUser(System.console(), "admin");
        if (readTurnFile() != "admin") throw new IllegalArgumentException("Game is already in progress. Cannot remove user.");
        manager.removeUser(u); // Remove user from the game
        Files.deleteIfExists(gameDir.resolve(u + ".txt")); // Delete user's file
    }

    public void startGame() throws IOException {
        manager.requireUser(System.console(), "admin");
        writeTurnFile("admin"); // Set the turn to admin
        loadUsers(); // Load users from the file

        if (manager.users.size() < 2) throw new IllegalArgumentException("Not enough players to start the game. At least 2 players are required.");


    }

    private void loadUsers() throws IOException {
        users.clear(); // Clear the list of users
        for (String line : manager.users.keySet()) {
            if (line.equals("admin")) continue; // Skip admin user
            users.add(new User(line, gameDir)); // Add user to the list
        }
    }
}
