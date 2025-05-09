public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        // Game.init("game1");
        Game game = new Game("game1");
        // game.addUser("user1");
        // game.removeUser("user2");
        game.startGame();
    }
}
