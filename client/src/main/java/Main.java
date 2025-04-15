import client.ServerFacade;
import exceptions.*;
import model.AuthData;
import model.UserData;

import java.util.Scanner;
import java.util.Arrays;

import static ui.EscapeSequences.*;

public class Main {
    public static void main(String[] args) {
        boolean loggedIn = false;
        AuthData currentAuth = null;


        Scanner scanner = new Scanner(System.in);
        ServerFacade facade = new ServerFacade("http://localhost:8080");

        System.out.print(ERASE_SCREEN);
        System.out.println("👑 Welcome to 240 Chess. Type help to get started. 👑\n");

        while (true) {
            System.out.print((loggedIn ? "[" + currentAuth.username() + "] " : "[LOGGED_OUT]") + " >>> ");

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
                        if (cmd_args.length != 3) {
                            System.out.println(
                                SET_TEXT_COLOR_RED +
                                "↪  `register` requires 3 arguments: <USERNAME> <PASSWORD> <EMAIL>" +
                                RESET_TEXT_COLOR
                            );
                            break;
                        }

                        try {
                            UserData userData = new UserData(cmd_args[0], cmd_args[1], cmd_args[2]);
                            currentAuth = facade.register(userData);
                            if (currentAuth != null) {
                                System.out.println(
                                    SET_TEXT_COLOR_GREEN +
                                    "↪  Successfully registered and logged in" +
                                    RESET_TEXT_COLOR
                                );
                            }
                        } catch (ConnectionException exception) {
                            System.out.println(
                                SET_TEXT_COLOR_RED +
                                "↪  Failed to connect to the server" +
                                RESET_TEXT_COLOR
                            );
                        } catch (BadRequestException exception) {
                            System.out.println(
                                SET_TEXT_COLOR_RED +
                                "↪  Failed to register and login: malformed request" +
                                RESET_TEXT_COLOR
                            );
                        } catch (GenericTakenException exception) {
                            System.out.println(
                                SET_TEXT_COLOR_RED +
                                "↪  Failed to register and login: username already taken" +
                                RESET_TEXT_COLOR
                            );
                        } catch (Exception exception) {
                            System.out.println(
                                SET_TEXT_COLOR_RED +
                                "↪  Failed to register and login: (" + exception.getClass() + ") " + exception.getMessage() +
                                RESET_TEXT_COLOR
                            );
                        }
                        break;
                    case "login":
                        if (cmd_args.length != 2) {
                            System.out.println(
                                SET_TEXT_COLOR_RED +
                                "↪  `register` requires 2 arguments: <USERNAME> <PASSWORD>" +
                                RESET_TEXT_COLOR
                            );
                            break;
                        }

                        try {
                            UserData userData = new UserData(cmd_args[0], cmd_args[1], null);
                            currentAuth = facade.login(userData);
                            if (currentAuth != null) {
                                System.out.println(
                                    SET_TEXT_COLOR_GREEN +
                                    "↪  Successfully logged in" +
                                    RESET_TEXT_COLOR
                                );
                            }
                        } catch (ConnectionException exception) {
                            System.out.println(
                                SET_TEXT_COLOR_RED +
                                "↪  Failed to connect to the server" +
                                RESET_TEXT_COLOR
                            );
                        } catch (BadRequestException exception) {
                            System.out.println(
                                SET_TEXT_COLOR_RED +
                                "↪  Failed to login: malformed request" +
                                RESET_TEXT_COLOR
                            );
                        } catch (UnauthorizedException exception) {
                            System.out.println(
                                SET_TEXT_COLOR_RED +
                                "↪  Failed to login: invalid username or password" +
                                RESET_TEXT_COLOR
                            );
                        } catch (Exception exception) {
                            System.out.println(
                                SET_TEXT_COLOR_RED +
                                "↪  Failed to login: (" + exception.getClass() + ") " + exception.getMessage() +
                                RESET_TEXT_COLOR
                            );
                        }
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