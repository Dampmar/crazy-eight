public class Card {
    public enum Suit { 
        HEARTS("H"), DIAMONDS("D"), CLUBS("C"), SPADES("S"); // Enum for card suits
        private final String suit;
        Suit(String suit) { this.suit = suit; }
        public String getSuit() { return suit; }

        public static Suit isValidSuit(String suit) { 
            for (Suit s : Suit.values()) { // Check if the rank is valid
                if (s.getSuit().equals(suit)) return s;
            }
            return null;
        }
    }

    public enum Rank { 
        TWO("2"), THREE("3"), FOUR("4"), FIVE("5"), SIX("6"), SEVEN("7"), EIGHT("8"), NINE("9"),
        TEN("10"), JACK("J"), QUEEN("Q"), KING("K"), ACE("A"); // Enum for card ranks
        private final String rank;
        Rank(String rank) { this.rank = rank; }
        public String getRank() { return rank; }

        public static Rank isValidRank(String rank) { 
            for (Rank r : Rank.values()) { // Check if the rank is valid
                if (r.getRank().equals(rank)) return r;
            }
            return null;
        }
    }

    private final Suit suit; // Card suit
    private final Rank rank; // Card rank

    /** Constructor: create a card with a suit and rank
     * @param suit Card suit
     * @param rank Card rank
     */
    public Card(Suit suit, Rank rank) { this.suit = suit; this.rank = rank; }

    /** isPlayable: check if the card is playable based on the current suit and rank
     * @param otherCard Card to compare with
     * @return true if the card is playable, false otherwise
     */
    public boolean isPlayable(Card otherCard) {
        return this.rank == otherCard.rank || this.suit == otherCard.suit || this.rank == Rank.EIGHT;
    }

    /** toString: get the string representation of the card for file saving
     * @return string representation of the card
     */
    @Override 
    public String toString() {
        return suit.getSuit() + rank.getRank(); // Format: "H2" for 2 of Hearts
    }

    /** fromString: create a card from its string representation
     * @param c string representation of the card
     */
    public static Card fromString(String c) {
        if (c == null || c.length() < 2 || c.length() > 3) throw new IllegalArgumentException("Invalid card string: " + c);

        String s = c.substring(0,1);
        String r = c.substring(1);

        // Check if the rank and suit are valid
        Suit suit = Suit.isValidSuit(s);
        Rank rank = Rank.isValidRank(r);

        // Make sure both are valid
        if (suit == null || rank == null) {
            throw new IllegalArgumentException("Invalid card string: " + c);
        }

        if (suit == null || rank == null) {
            throw new IllegalArgumentException("Invalid card string: " + c);
        }

        return new Card(suit, rank);
    }

    /** equals: check if two cards are equal based on their suit and rank
     * @param obj object to compare with
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true; 
        if (obj == null || getClass() != obj.getClass()) return false; 
        Card otherCard = (Card) obj; 
        return suit == otherCard.suit && rank == otherCard.rank; 
    }

    public Rank getRank() { return rank; }
}