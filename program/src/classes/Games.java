package classes;

import java.util.*;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Games {
    private static final String GAMES_DIR = "src" + File.separator + "games" + File.separator;
    private static String gameName; 
    
    public static void initializeGame(String gameName) {
        gameName = gameName;
        File gameDir = new File(GAMES_DIR + gameName);

        if (!gameDir.exists()) {
            gameDir.mkdirs();
            File usersFile = new File(gameDir, "users.txt");
            try {
                usersFile.createNewFile();
                String adminPassword = readPasswordFromConsole();
                String hashedPassword = hashPassword(adminPassword);
                writeAdminUserToFile(usersFile, hashedPassword);
            } catch (IOException e) {
                System.err.println("Error creating game files: " + e.getMessage());
            }
        } else {
            System.out.println("ERROR: Cannot create game; game already exists.");
        }
    }

    private static String readPasswordFromConsole() {
        Console console = System.console();
        if (console == null) {
            System.out.println("Couldn't get Console instance, maybe running in an IDE. Please enter password:");
            Scanner scanner = new Scanner(System.in);
            return scanner.nextLine();
        }
        return new String(console.readPassword("Enter admin password: "));
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("SHA3-256 algorithm not available: " + e.getMessage());
            return null;
        }
    }

    private static void writeAdminUserToFile(File usersFile, String hashedPassword) {
        try (FileWriter writer = new FileWriter(usersFile)) {
            writer.write("admin," + hashedPassword);
        } catch (IOException e) {
            System.err.println("Error writing admin user to file: " + e.getMessage());
        }
    }
}
