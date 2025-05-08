package components;

import java.util.*;
import java.io.*;
import java.nio.file.*;

public class User {
    private final String username;
    private final Path userPath; // Path to the user's file
    private final List<Card> hand = new ArrayList<>(); // User's hand of cards

    public User(String username, Path dir) throws IOException {
        if (username.equalsIgnoreCase("admin")) throw new IllegalArgumentException("Username 'admin' is reserved.");
        this.username = username; 
        this.userPath = dir.resolve(username + ".txt");
        if (Files.exists(userPath)) loadHand(); 
    }

    public String getUsername() { return username; } // Get the username
    public List<Card> getHand() { return hand; } // Get the user's hand

    /** addCard: add a card to the user's hand
     * @param card Card to add to the hand
     */
    public void addCard(Card card) {
        hand.add(card);
        saveHand();
    }

    /** playCard: remove a card from the user's hand
     * @param card Card to remove from the hand
     */
    public void playCard(Card card, Card topCard) {
        if (!hand.contains(card)) throw new IllegalArgumentException("Card not in hand: " + card);
        if (!card.isPlayable(topCard)) throw new IllegalArgumentException("Card not playable: " + card);
        hand.remove(card);
        saveHand();
    }

    /** loadHand: load a hand from a file */
    private void loadHand() throws IOException {
        hand.clear();
        for (String line : Files.readAllLines(userPath)) {
            hand.add(Card.fromString(line.trim()));
        }
    }

    /** saveHand: save the user's hand to a file */
    public void saveHand() {
        try {
            List<String> lines = new ArrayList<>();
            for (Card c : hand) lines.add(c.toString());
            Files.write(userPath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) { throw new RuntimeException("Error saving hand: " + e.getMessage(), e); }
    }

    public boolean hasWon() { return hand.isEmpty(); } // Check if the user has won

    public int score() { // Calculate the score based on the cards in hand
        int score = 0;
        for (Card c : hand) {
            Rank rank = c.getRank();
            if (rank == Rank.JACK || rank == Rank.QUEEN || 
                rank == Rank.KING || rank == Rank.ACE) {
                score += 10; // Face cards and Ace are worth 10 points
            } else {
                // Convert the rank string to a number for numeric cards
                try {
                    score += Integer.parseInt(rank.getRank());
                } catch (NumberFormatException e) {
                    // This should not happen with properly formatted cards
                    System.err.println("Invalid rank: " + rank.getRank());
                }
            }
        }
        return score;
    }
}
