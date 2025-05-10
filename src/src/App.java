public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        // Game.init("game1");  // Uncomment to initialize a new game
        Game game = new Game("game1"); 
        //game.addUser("user1"); // Uncomment to add a user
        //game.addUser("user2"); // Uncomment to add a user
        //game.removeUser("user2"); // Uncomment to remove a user
        // game.startGame(); // Uncomment to start the game
        //game.getTurnOrder("user2");
        //game.getCards("user1", "user1");
        // game.drawCard("user1");
        // game.passTurn("user1");
    }
}
