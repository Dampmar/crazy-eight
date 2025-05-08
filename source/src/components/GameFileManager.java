package components;

import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.nio.charset.StandardCharsets;

public class GameFileManager {
    // Directory to store all game data
    private final Path usersPath;
    public final Map<String, String> users = new HashMap<>(); // Map to store usernames and their hashed passwords

    /** Constructor: retrieve contents based on directory
     * @param gameDir
     */
    public GameFileManager(Path gameDir) throws IOException {
        this.usersPath = gameDir.resolve("users.txt");
        if (Files.exists(usersPath)) {
            for (String line : Files.readAllLines(usersPath)) {
                String[] parts = line.split(",",2);
                if (parts.length == 2) { users.put(parts[0], parts[1]); }
            }
        }
    }

    /** addUser: add a new user to the usersFile
     * @param console Console instance to get password
     * @param username Username to add
     */
    public void addUser(Console console, String username) throws IOException {
        // Check for edge cases (e.g. invalid usernames) => throw exception 
        if (username == null || username.isEmpty()) throw new IllegalArgumentException("Username cannot be null or empty.");
        if (username.equalsIgnoreCase("admin")) throw new IllegalArgumentException("Username 'admin' is reserved.");
        if (users.containsKey(username)) throw new IllegalArgumentException("Username already exists.");
        if (users.size() > 10) throw new IllegalArgumentException("Maximum number of users reached (10).");

        // Get password from console
        String password = getPassword(console);
        String hashedPassword = hashPassword(password);

        // Add user to the map and save to file
        users.put(username, hashedPassword);
        saveUsersToFile();
    }

    /** removeUser: remove a user from the usersFile 
     * @param username Username to remove
     */
    public void removeUser(String username) throws IOException {
        // Check for edge cases (e.g. invalid usernames) => throw exception 
        if (username == null || username.isEmpty()) throw new IllegalArgumentException("Username cannot be null or empty.");
        if (!users.containsKey(username)) throw new IllegalArgumentException("Username does not exist.");
        if (username.equalsIgnoreCase("admin")) throw new IllegalArgumentException("Cannot remove the 'admin' user.");

        // Remove user from the map and save to file
        users.remove(username);
        saveUsersToFile();
    }

    /** initializeAdmin: create the admin user for a new game
     * @param console Console instance to get password
     */
    public void initializeAdmin(Console console) throws IOException {
        if (!users.isEmpty()) throw new IllegalStateException("Game already exists.");
        String password = getPassword(console);
        String hashedPassword = hashPassword(password);
        users.put("admin", hashedPassword);
        saveUsersToFile();
    }

    /** requireUser: check if a user exists and verify the password
     * @param console Console instance to get password
     * @param username Username to check
     * @return true if the user exists and the password is correct, false otherwise
     */
    public boolean requireUser(Console console, String username) throws IOException {
        // Check for edge cases (e.g. invalid usernames) => throw exception 
        if (username == null || username.isEmpty()) throw new IllegalArgumentException("Username cannot be null or empty.");
        if (!users.containsKey(username)) throw new IllegalArgumentException("User doesn't exist."); // User does not exist

        // Get password from console and verify
        String password = getPassword(console);
        String hashedPassword = hashPassword(password);
        if (!hashedPassword.equals(users.get(username))) throw new IllegalArgumentException("Password is incorrect."); // Password is incorrect
        
        return true; // Password is correct
    }

    /** saveUserToFile: save the users map to the usersFile
     * @throws IOException if an I/O error occurs
     */
    private void saveUsersToFile() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(usersPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        }
    }

    /** getPassword: get a password from the console
     * @param console Console instance to get password
     * @return the password entered by the user
     */
    private static String getPassword(Console console) {
        if (console == null) throw new IllegalAccessError("Console not available.");
        char[] password = console.readPassword("Enter admin password: ");
        return new String(password);
    }

    /** hashPassword: hash a password using SHA3-256
     * @param password Password to hash
     * @return the hashed password as a hex string
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password: ", e);
        }
    }
}