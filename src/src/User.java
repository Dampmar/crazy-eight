import java.util.*;
import java.io.*;
import java.nio.file.*;

public class User {
    private final String username;
    private final Path userFilePath;
    private final List<Card> hand = new ArrayList<>();

    public User(String username, Path gameDir) throws IOException {
        if (username.equalsIgnoreCase("admin")) throw new IllegalArgumentException("Username 'admin' is reserved.");
        this.username = username;
        this.userFilePath = gameDir.resolve(username + ".txt");
        if (Files.exists(userFilePath)) loadHand();
    }

    /** drawCard: add a card to the user's hand
     * * @param card the card to add
     */
    public void drawCard(Card card) {
        hand.add(card);
        saveHand();
    } 

    /** discardCard: remove a card from the user's hand
     * * @param card the card to remove
     */
    public void discardCard(Card card) {
        hand.remove(card);
        saveHand();
    } 

    /** getHand: get the user's hand
     * * @return the user's hand
     */
    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    /** saveHand: save the user's hand to a file */
    private void saveHand() {
        try {
            List<String> lines = new ArrayList<>();
            for (Card c : hand) lines.add(c.toString());
            Files.write(userFilePath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) { throw new RuntimeException("Error saving hand: " + e.getMessage(), e); }
    }

    /** loadHand: load a hand from a file */
    private void loadHand() throws IOException {
        hand.clear();
        for (String line : Files.readAllLines(userFilePath)) {
            hand.add(Card.fromString(line.trim()));
        }
    }

    /** getUsername: get the username of the user
     * * @return the username of the user
     */
    public String getUsername() { return username; }

    /** hasWon: check if the user has an empty hand
     * * @return true if the user has won, false otherwise
     */
    public boolean hasWon() { return hand.isEmpty(); } 
}
