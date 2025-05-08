package utils;

import java.util.*;

public class ParseArgs {
    public static Map<String, String> parseArgs(String[] args) {
        Map<String,String> params = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            switch(args[i]) {
                case "--init":
                    params.put("init",""); break;
                case "--game":
                    params.put("game",args[++i]); break;
                case "--add-user": 
                    params.put("add-user",args[++i]);break;
                case "--remove-user":
                    params.put("remove-user",args[++i]); break;
                case "--start":
                    params.put("start",""); break;
                case "--order":
                    params.put("order",""); break;
                case "--user":
                    params.put("user",args[++i]); break;
                case "--cards":
                    params.put("cards",args[++i]); break;
                case "--play":
                    params.put("play",args[++i]); break;
                case "--draw":
                    params.put("draw",""); break;
                default:
                    System.out.println("Unknown argument: " + args[i]);
                    break;
            }
        }
        if (!params.containsKey("game")) throw new IllegalArgumentException("Game not specified. Use --game <game_name>");
        return params;
    }
}
