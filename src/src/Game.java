import java.util.*;
import java.util.stream.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class Game {
    private static final String GAMES_FOLDER = "games";
    private final Path gameDir;
    private final Path deckFile; // Deck file
    private final Path discardFile; // Deck file
    private final Path turnFile;
    private final GameManager manager;

    /** Constructor: initializes the game directory if exists
     * * @param name the name of the game
     */
    public Game(String name) throws IOException {
        this.gameDir = Paths.get(GAMES_FOLDER, name);
        this.turnFile = gameDir.resolve("turn.txt");
        this.discardFile = gameDir.resolve("discard.txt");
        this.deckFile = gameDir.resolve("deck.txt");
        if (!Files.exists(gameDir)) throw new IllegalArgumentException("Game directory does not exist: " + name);
        if (!Files.exists(turnFile)) throw new IllegalArgumentException("Turn file does not exist: " + turnFile);
        if (!Files.exists(deckFile)) throw new IllegalArgumentException("Deck file does not exist: " + deckFile);
        if (!Files.exists(discardFile)) throw new IllegalArgumentException("Discard file does not exist: " + discardFile);
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

        // Create other files 
        Path discFile =  gameDir.resolve("discard.txt");
        Path deckFile =  gameDir.resolve("deck.txt");
        Files.createFile(discFile);
        Files.createFile(deckFile);
        
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

    /**
     * * startGame: starting or resetting a game
     * Starting a game means shuffling the deck and dealing cards to players.
     */
    public void startGame() throws IOException {
        // Check if the game is already started and validate that the user starting the game is an admin
        manager.requireUser("admin");
        if (!readTurn().equals("admin")) throw new IllegalStateException("Game already started: " + turnFile);

        // Get the list of users from the game manager excluding the admin
        List<String> usernames = new ArrayList<>();
        for (String username : manager.users.keySet()) {
            if (username.equals("admin")) continue;
            usernames.add(username);
        }
        
        if (usernames.size() < 2) throw new IllegalStateException("Not enough players to start the game: " + usernames.size());
        
        // Create the game files for each user
        List<User> users = new ArrayList<>();
        for (String username : usernames) users.add(new User(username, gameDir));
        
        // Create the deck and shuffle it 
        List<Card> deck = createShuffledDeck();
        List<Card> discard = new ArrayList<>(); // Create the discard pile

        // Deal 5 cards to each player
        for (User user : users) {
            user.drawCard(deck.remove(0)); // Draw a card from the deck
            user.drawCard(deck.remove(0)); // Draw a card from the deck
            user.drawCard(deck.remove(0)); // Draw a card from the deck
            user.drawCard(deck.remove(0)); // Draw a card from the deck
            user.drawCard(deck.remove(0)); // Draw a card from the deck
        }

        String firstPlayer = users.get(0).getUsername(); // Get the first player
        writeDecks(deck, discard);
        writeTurn(firstPlayer);
    }

    /** createShuffledDeck: create a shuffled deck of cards
     * * @return a shuffled deck of cards
     */
    private List<Card> createShuffledDeck() {
        List<Card> deck = new ArrayList<>();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(suit, rank)); // Add each card to the deck
            }
        }
        Collections.shuffle(deck); // Shuffle the deck
        return deck;
    }

    /** writeDecks: write the deck and discard pile to files
     * * @param deck the deck of cards
     * * @param discard the discard pile
     */
    private void writeDecks(List<Card> deck, List<Card> discard) throws IOException {
        if (!Files.exists(deckFile)) throw new IllegalAccessError("Deck file doesn't exist"); 
        if (!Files.exists(discardFile)) throw new IllegalAccessError("Discard file doesn't exist"); 

        // Write the deck to the deck file
        try (BufferedWriter writer = Files.newBufferedWriter(deckFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Card card : deck) {
                writer.write(card.toString());
                writer.newLine();
            }
        }

        // Write the discard pile to the discard file
        try (BufferedWriter writer = Files.newBufferedWriter(discardFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Card card : discard) {
                writer.write(card.toString());
                writer.newLine();
            }
        }
    }

    /** getDeck: get the deck of cards
     * * @return the deck of cards
     */
    public List<Card> getDeck() throws IOException {
        if (!Files.exists(deckFile)) throw new IllegalAccessError("Deck file doesn't exist"); 
        List<Card> deck = new ArrayList<>();
        for (String line : Files.readAllLines(deckFile)) {
            deck.add(Card.fromString(line.trim())); // Add each card to the deck
        }
        return deck;
    }

    /** getDiscard: get the discard pile
     * * @return the discard pile
     */
    public List<Card> getDiscard() throws IOException {
        if (!Files.exists(discardFile)) throw new IllegalAccessError("Discard file doesn't exist"); 
        List<Card> discard = new ArrayList<>();
        for (String line : Files.readAllLines(discardFile)) {
            discard.add(Card.fromString(line.trim())); // Add each card to the discard pile
        }
        return discard;
    }


}