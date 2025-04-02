import chess.*;
import client.ServerFacade;

import java.util.Scanner;
import java.util.Arrays;

import static ui.EscapeSequences.ERASE_SCREEN;

public class Main {
    public static void main(String[] args) {
        boolean loggedIn = false;
        String currentUser = null;
        Scanner scanner = new Scanner(System.in);
        ServerFacade facade = new ServerFacade("http://localhost:8080");

        System.out.print(ERASE_SCREEN);
        System.out.println("👑 Welcome to 240 Chess. Type help to get started. 👑\n");

        while (true) {
            System.out.print((loggedIn ? "[" + currentUser + "] " : "[LOGGED_OUT]") + " >>> ");

            // Get user input
            String line = scanner.nextLine();
            String[] split_line = line.split(" ");

            // Create command values
            String cmd_name = split_line[0].toLowerCase();
            String[] cmd_args = Arrays.copyOfRange(split_line, 1, split_line.length);

            if (!loggedIn) {
                switch (cmd_name) {
                    case "help":
                        System.out.println("↪ AVAILABLE COMMANDS:");
                        System.out.println("↪  register <USERNAME> <PASSWORD> <EMAIL>");
                        System.out.println("↪  login <USERNAME> <PASSWORD>");
                        System.out.println("↪  help");
                        System.out.println("↪  quit");
                        break;
                    case "register":

                        break;
                    case "login":
                        break;
                    case "quit":
                        System.out.println("↪ Thanks for playing!");
                        return;
                    default:
                        System.out.println("↪ Invalid Command!");
                }
            } else {
                switch (cmd_name) {
                    case "help":
                        System.out.println("↪ AVAILABLE COMMANDS:");
                        System.out.println("↪  create <NAME> - Create a game");
                        System.out.println("↪  list - Get a list of games");
                        System.out.println("↪  join <id> [WHITE|BLACK] - Play a game");
                        System.out.println("↪  observe <id> - Observe a game");
                        System.out.println("↪  logout");
                        System.out.println("↪  help");
                        System.out.println("↪  quit");
                        break;
                    case "create":
                        break;
                    case "list":
                        break;
                    case "join":
                        break;
                    case "observe":
                        break;
                    case "logout":
                        break;
                    case "quit":
                        System.out.println("↪ Thanks for playing!");
                        return;
                    default:
                        System.out.println("↪ Invalid Command!");
                }
            }
        }
    }
}