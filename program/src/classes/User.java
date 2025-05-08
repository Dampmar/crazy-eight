package classes;

public class User {
    public String username;
    public String hashedPassword;

    // Constructor
    public User(String username, String hash) {
        this.username = username;
        this.hashedPassword = hash;
    }

    // To String Method to save the user to a file 
    @Override 
    public String toString() {
        return username + "," + hashedPassword;
    }
}
