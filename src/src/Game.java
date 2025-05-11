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
        String turn = new String(Files.readAllBytes(turnFile), StandardCharsets.UTF_8);
        turn = turn.trim();
        turn = turn.split(",")[0];
        return new String(turn);
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
        discard.add(deck.remove(0));    // Draw a card from the deck and add it to the discard pile
        writeDecks(deck, discard);
        writeTurn(firstPlayer + ",0");        // Write the first player's turn to the turn file, hasn't drawn yet
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

    /** getTurnOrder: get the list of players in turn order, starting who's turn it is 
     * * @param requesterUsername the username of the player requesting the turn order
     */
    public void getTurnOrder(String requesterUsername) throws IOException {
        // Verify that the requester user exists 
        manager.requireUser(requesterUsername);

        // Read the current turn from the turn file
        String currentPlayer = readTurn();
        if (currentPlayer.equals("admin")) throw new IllegalStateException("Game not started yet!");

        // Get all players except the admin 
        List<String> players = new ArrayList<>();
        for (String username : manager.users.keySet()) {
            if (username.equals("admin")) continue;
            players.add(username);
        }

        // Reorder the list of players based on the current turn
        List<String> turnOrder = new ArrayList<>();
        int currentTurn = players.indexOf(currentPlayer);

        // First add the current player and players after 
        for (int i = currentTurn; i < players.size(); i++) turnOrder.add(players.get(i));

        // Then add the players before the current player 
        for (int i = 0; i < currentTurn; i++) turnOrder.add(players.get(i));

        // Print the turn order
        System.out.println("Turn order: ");
        for (int i = 0; i < turnOrder.size(); i++) {
            System.out.println((i + 1) + ". " + turnOrder.get(i));
        }
    }

    /** getCards: get the cards of a player and the discard pile top card
     * * @param userToGet the username of the user to get cards from
     * * @param requesterUsername the username of the player requesting the cards
     */
    public void getCards(String userToGet, String requesterUsername) throws IOException {
        // Verify that the requester user exists 
        manager.requireUser(requesterUsername);

        // Verify that the user has access to the cards
        if (!(requesterUsername.equals(userToGet) || requesterUsername.equals("admin"))) {
            throw new SecurityException("You don't have access to this user's cards: " + userToGet);
        }

        // Verify that the game has started 
        String currentPlayer = readTurn();
        if (currentPlayer.equals("admin")) throw new IllegalStateException("Game not started yet!");
    
        // Get the user's cards
        User user = new User(userToGet, gameDir);
        List<Card> userCards = user.getHand();
        Card topCard = getTopCardFromDiscard();

        // Print the user's cards
        System.out.println("Cards of " + userToGet + ": ");
        for (int i = 0; i < userCards.size(); i++) {
            System.out.println(" " + (i+1) + ". " + userCards.get(i));
        }

        // Print the top card of the discard pile
        System.out.println("\nTop card of the discard pile: " + topCard);
    }

    /** getTopCardFromDiscard: get the top card of the discard pile
     * * @return the top card of the discard pile 
     */
    private Card getTopCardFromDiscard() throws IOException {
        List<Card> discard = getDiscard();
        if (discard.isEmpty()) return null;
        return discard.get(discard.size() - 1);
    }

    /** drawCard: draw a card from the deck and add it to the user's hand
     * * @param username the username of the user drawing the card
     */
    public void drawCard(String username) throws IOException {
        manager.requireUser(username);
        String currentPlayer = readTurn();
        
        // Verify that the game has started, the user is the current player, and the user has not drawn a card yet
        if (currentPlayer.equals("admin")) throw new IllegalStateException("Game not started yet!");
        if (!currentPlayer.equals(username)) throw new IllegalStateException("It's not your turn: " + currentPlayer);
        if (hasDrawn(username)) throw new IllegalStateException("You have already drawn a card: " + username);

        // Get the user and the deck
        User user = new User(username, gameDir);
        List<Card> deck = getDeck();
        List<Card> discard = getDiscard();

        // Reshuffle the deck if it's empty
        if (deck.isEmpty()) {
            System.out.println("Deck is empty, reshuffling the discard pile into the deck...");

            // Highly unlikely to happen: 2/10 players draw card from the get go
            Card topCard = discard.remove(discard.size() - 1); 
            if (discard.isEmpty()) throw new IllegalStateException("Discard pile is empty, cannot reshuffle.");

            // Add the remaining cards from the discard pile to the deck
            deck.addAll(discard);
            discard.clear(); 
            discard.add(topCard); 
            Collections.shuffle(deck);
        }

        // Draw a card from the deck and add it to the user's hand
        Card drawnCard = deck.remove(deck.size() - 1);
        user.drawCard(drawnCard);
        writeTurn(username + "," + 1);          
        writeDecks(deck, discard);  
    }

    /** cannotDrawCard: check if the user has already drawn a card
     * * @param username the username of the user
     * * @return true if the user has already drawn a card, false otherwise
     */
    private boolean hasDrawn(String username) throws IOException {
        if (!Files.exists(turnFile)) throw new IllegalAccessError("Turn file doesn't exist"); 
        String line = new String(Files.readAllBytes(turnFile), StandardCharsets.UTF_8);
        line = line.trim();
        String[] parts = line.split(",");
        if (parts[1].equals("0")) return false;
        else if (parts[1].equals("1")) return true;
        else throw new IllegalStateException("Invalid turn file format: " + turnFile);
    }

    /** passTurn: pass the turn to the next player
     * * @param username the username of the user passing the turn
     */
    public void passTurn(String username) throws IOException {
        manager.requireUser(username);
        String currentPlayer = readTurn();
        
        // Verify that the game has started and the user is the current player
        if (currentPlayer.equals("admin")) throw new IllegalStateException("Game not started yet!");
        if (!currentPlayer.equals(username)) throw new IllegalStateException("It's not your turn: " + currentPlayer);
        if (!hasDrawn(username)) throw new IllegalStateException("You haven't drawn a card: " + username + " cannot pass the turn.");

        // Get the list of players from the game manager excluding the admin
        List<String> players = new ArrayList<>();
        for (String player : manager.users.keySet()) {
            if (player.equals("admin")) continue;
            players.add(player);
        }

        // Find the index of the current player and pass the turn to the next player
        int currentIndex = players.indexOf(currentPlayer);
        int nextIndex = (currentIndex + 1) % players.size();
        String nextPlayer = players.get(nextIndex);
        writeTurn(nextPlayer + ",0"); 
    }

    /** playCard: play card from deck 
     * * @param username the username of the user playing a card
     */
    public void playCard(String username, String cardString) throws IOException {
        manager.requireUser(username);
        String currentPlayer = readTurn();

        // Verify that the game has started and the user is the current player
        if (currentPlayer.equals("admin")) throw new IllegalStateException("Game not started yet!");
        if (!currentPlayer.equals(username)) throw new IllegalStateException("It's not your turn: " + username);

        // Get the user and the discard pile 
        User user = new User(username, gameDir);
        List<Card> discard = getDiscard();
        List<Card> userCards = user.getHand();
        Card topCard = getTopCardFromDiscard();

        // Check if the card is in the user's hand
        Card cardToPlay = null;
        for (Card card : userCards) {
            if (card.toString().equals(cardString)) {
                cardToPlay = card;
                break;
            }
        }
        
        // Check if the card is playable and in the user's hand
        if (cardToPlay == null) throw new IllegalArgumentException("Card not found in hand: " + cardString);
        if (!cardToPlay.isPlayable(topCard)) throw new IllegalArgumentException("Card not playable: " + cardToPlay + " on top of " + topCard);

        // Remove the card from the user's hand and add it to the discard pile
        user.discardCard(cardToPlay);
        discard.add(cardToPlay);
        writeDecks(getDeck(), discard); // Write the updated deck and discard pile to files

        // Get the list of players from the game manager excluding the admin
        List<String> players = new ArrayList<>();
        for (String player : manager.users.keySet()) {
            if (player.equals("admin")) continue;
            players.add(player);
        }

        // Find the index of the current player and pass the turn to the next player
        int currentIndex = players.indexOf(currentPlayer);
        int nextIndex = (currentIndex + 1) % players.size();
        String nextPlayer = players.get(nextIndex);
        writeTurn(nextPlayer + ",0"); 

        // If the use has won the game reset the game, and delete or clear the game files depending on the functionality
        if (user.hasWon()) {
            System.out.println("User " + username + " has won the game!");

            // Delete all player hand files
            for (String player : players) {
                if (player.equals("admin")) continue;
                Files.deleteIfExists(gameDir.resolve(player + ".txt"));
            }
            
            // Clear deck and discard files but keep them
            Files.write(deckFile, new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.write(discardFile, new byte[0], StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // Set turn to admin 
            writeTurn("admin");
        }
    }
}