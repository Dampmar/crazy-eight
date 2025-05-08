package classes;

import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.stream.*;

public class GameManager {
    private final Path filePath;                    // Path to the users file
    private final List<User> users = new ArrayList<>();   // List to store users

    // Constructor to initialize the file manager with a directory
    public GameManager(Path dir) throws IOException {
        filePath = dir.resolve("users.txt");
        if (Files.exists(filePath)) {
            for (String line : Files.readAllLines(filePath)) {
                String[] parts = line.split(",",2);
                if (parts.length == 2) {
                    String username = parts[0].trim();
                    String hash = parts[1].trim();
                    users.add(new User(username, hash)); // Add user to the list
                }
            }
        }
    }

    /** Add user: check if user already exists, add to list and save to file
     * @param console Console instance to read password
     * @param username the username to add
     */
    public void addUser(Console console, String username) throws IOException {
        // Check for edge cases (e.g. the user already exists) => throw an exception
        if (username == null || username.isEmpty()) throw new IllegalArgumentException("Invalid username: " + username);
        if (username.equalsIgnoreCase("admin")) throw new IllegalArgumentException("Reserved user: " + username);
        if (users.stream().anyMatch(user -> user.username.equals(username))) throw new IllegalArgumentException("User already exists: " + username);
        if (users.size() > 10) throw new IllegalStateException("Maximum number of users reached (10), cannot add more users.");

        // Ask for the password and hash it
        String password = getPassword(console);
        String hashedPassword = hashPassword(password);
        users.add(new User(username, hashedPassword));
        saveUsers(); // Save the new user to the file
    }


    /** Remove user: check if user exists, remove from list and save to file
     * @param username the username to remove 
     */
    public void removeUser(String username) throws IOException {
        // Check if the user doesn't exist or that the username isn't "admin" => throw an exception
        if (username.equalsIgnoreCase("admin")) throw new IllegalArgumentException("Cannot remove 'admin' user.");
        if (users.stream().noneMatch(user -> user.username.equals(username))) throw new IOException("Invalid user: " + username);
        users.removeIf(user -> user.username.equals(username));
        saveUsers();
    }
    
    /** Initialize Admin User: check if the game is already initialized, if not, create an admin user
     * @param console Console instance to read password
     */
    public void initializeAdmin(Console console) throws IOException {
        if (!users.isEmpty()) throw new IllegalAccessError("Game already initialized!");
        String password = getPassword(console);
        String hashedPassword = hashPassword(password);
        users.add(new User("admin", hashedPassword)); // Add admin user to the list
        saveUsers(); // Save the new user to the file
    }

    /** Validate User
     * @param username the username to validate
     */
    public void validateUser(String username, Console console) {
        // Check if the username is null or empty => throw an exception
        User user = users.stream()
                        .filter(u -> u.username.equals(username))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        String password = getPassword(console);
        String hashedPassword = hashPassword(password);
        if (!hashedPassword.equals(user.hashedPassword)) throw new SecurityException("Invalid password for user: " + username);
    }

    /**
     * Method to save users state to the file
     * @throws IOException if an error occurs while writing to the file
     */
    private void saveUsers() throws IOException {
        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            for (User user : users) {
                writer.write(user.toString() + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new IOException("Error saving users to file: " + e.getMessage(), e);
        }
    }

    private static String getPassword(Console console) {
        if (console == null) throw new IllegalAccessError("Console not available.");
        char[] password = console.readPassword("Enter admin password: ");
        return new String(password);
    }
    
    private static String hashPassword(String pwd) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            byte[] hash = digest.digest(pwd.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password: ", e);
        }
    }
}
