import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.security.*;
import java.nio.charset.StandardCharsets;

public class GameManager {
    private final Path usersFilePath;
    public final Map<String, String> users = new HashMap<>();

    /** Constructor: retrieve contents based on directory
     * * @param gameDir
     */
    public GameManager(Path gameDir) throws IOException {
        this.usersFilePath = gameDir.resolve("users.txt");
        if (Files.exists(usersFilePath)) {
            for (String line : Files.readAllLines(usersFilePath)) {
                String[] parts = line.split(",",2);
                if (parts.length == 2) { users.put(parts[0], parts[1]); }
            }
        }
    }

    /** initGame: create the admin user and save the current state
     * * @param console Console to read user input
     */
    public void initGame(Console console) throws IOException {
        if (!users.isEmpty()) throw new IllegalStateException("Game already initialized: " + usersFilePath);
        String password = getPassword(console, "admin");
        String hashedPassword = hashPassword(password);
        users.put("admin", hashedPassword);
        saveUsers();
    }

    /** getPassword: read password from console
     * * @param console Console to read user input
     * * @return the password entered by the user
     */
    private static String getPassword(Console console, String username) {
        if (console == null) throw new IllegalAccessError("Console not available.");
        char[] password = console.readPassword("Enter " + username + " password: ");
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

    /** saveUsers: save the users map to the users file 
     * * @throws IOException if an I/O error occurs
     */
    private void saveUsers() throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(usersFilePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                writer.write(entry.getKey() + "," + entry.getValue());
                writer.newLine();
            }
        }
    }

    /** addUser: add a new user to the game
     * * @param username the username of the new user
     */
    public void addUser(String username) throws IOException {
        // Check for edge cases (e.g. invalid usernames) => throw exception 
        if (username == null || username.isEmpty()) throw new IllegalArgumentException("Username cannot be null or empty.");
        if (username.equalsIgnoreCase("admin")) throw new IllegalArgumentException("Username cannot be 'admin'.");
        if (username.equalsIgnoreCase("deck")) throw new IllegalArgumentException("Username cannot be 'deck'.");
        if (username.equalsIgnoreCase("discard")) throw new IllegalArgumentException("Username cannot be 'discard'.");
        if (username.equalsIgnoreCase("turn")) throw new IllegalArgumentException("Username cannot be 'turn'.");
        if (users.containsKey(username)) throw new IllegalArgumentException("Username already exists: " + username);
        if (users.size() > 10) throw new IllegalArgumentException("Maximum number of users reached (10).");

        // Get password from console
        String password = getPassword(System.console(), username);
        String hashedPassword = hashPassword(password);
        users.put(username, hashedPassword);
        saveUsers();
    }

    /** removeUser: remove a user from the game 
     * * @param username the username of the user to remove
     */
    public void removeUser(String username) throws IOException {
        // Check for edge cases (e.g. invalid usernames) => throw exception 
        if (username == null || username.isEmpty()) throw new IllegalArgumentException("Username cannot be null or empty.");
        if (!users.containsKey(username)) throw new IllegalArgumentException("Username does not exist.");
        if (username.equalsIgnoreCase("admin")) throw new IllegalArgumentException("Cannot remove the 'admin' user.");

        // Remove user from the map and save to file
        users.remove(username);
        saveUsers();
    }

    /** requireUser: check if a user exists and verify the password
     * @param username Username to check
     * @return true if the user exists and the password is correct, false otherwise
     */
    public boolean requireUser(String username) throws IOException {
        // Check for edge cases (e.g. invalid usernames) => throw exception 
        if (username == null || username.isEmpty()) throw new IllegalArgumentException("Username cannot be null or empty.");
        if (!users.containsKey(username)) throw new IllegalArgumentException("User doesn't exist."); // User does not exist

        // Get password from console and verify
        String password = getPassword(System.console(), username);
        String hashedPassword = hashPassword(password);
        if (!hashedPassword.equals(users.get(username))) throw new IllegalArgumentException("Password is incorrect."); // Password is incorrect
        
        return true; // Password is correct
    }
}
