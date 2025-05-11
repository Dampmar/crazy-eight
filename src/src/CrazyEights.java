import java.util.*;

public class CrazyEights {
    public static void main(String[] args) {
        try {
            Map<String, String> flags = parseArgs(args);
            // Initialize game
            if (flags.containsKey("init") && flags.containsKey("game")) {
                Game.init(flags.get("game"));
            } else {
                // Get game instance
                Game game = new Game(flags.get("game"));
                if (flags.containsKey("add-user")) { 
                    game.addUser(flags.get("add-user"));
                } else if (flags.containsKey("remove-user")) {
                    game.removeUser(flags.get("remove-user"));
                } else if (flags.containsKey("start")) {
                    game.startGame();
                } else if (flags.containsKey("order") && flags.containsKey("user")) {
                    game.getTurnOrder(flags.get("user"));
                } else if (flags.containsKey("play") && flags.containsKey("user")) {
                    game.playCard(flags.get("user"), flags.get("play"));
                } else if (flags.containsKey("cards") && flags.containsKey("user")) {
                    game.getCards(flags.get("cards"), flags.get("user"));
                } else if (flags.containsKey("draw") && flags.containsKey("user")) {
                    game.drawCard(flags.get("user"));
                } else if (flags.containsKey("pass") && flags.containsKey("user")) {
                    game.passTurn(flags.get("user"));
                } else {
                    throw new IllegalArgumentException("Invalid command or missing arguments.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> flags = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--init": 
                    flags.put("init",""); 
                    break;
                case "--game": 
                    flags.put("game", args[++i].trim()); 
                    break;
                case "--add-user": 
                    flags.put("add-user", args[++i].trim()); 
                    break;
                case "--remove-user": 
                    flags.put("remove-user", args[++i].trim()); 
                    break;
                case "--start": 
                    flags.put("start", ""); 
                    break;
                case "--order": 
                    flags.put("order", ""); 
                    break;
                case "--user": 
                    flags.put("user", args[++i].trim()); 
                    break;
                case "--play": 
                    flags.put("play", args[++i].trim()); 
                    break;
                case "--draw": 
                    flags.put("draw", ""); 
                    break;
                case "--pass": 
                    flags.put("pass", ""); 
                    break;
                case "--cards": 
                    flags.put("cards", args[++i].trim()); 
                    break;
                default: 
                    throw new IllegalArgumentException("Unknown argument: " + args[i]);
            }
        }
        if (!flags.containsKey("game")) {
            throw new IllegalArgumentException("Game name is required. Use --game <game_name>");
        }
        return flags;
    }
}
