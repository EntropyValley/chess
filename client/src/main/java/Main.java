import client.ServerFacade;
import exceptions.*;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Scanner;
import java.util.Arrays;

import static ui.EscapeSequences.*;

public class Main {
    public static void main(String[] args) {
        boolean loggedIn = false;
        AuthData currentAuth = null;


        Scanner scanner = new Scanner(System.in);
        ServerFacade facade = new ServerFacade("http://localhost:8081");

        System.out.print(ERASE_SCREEN);
        System.out.println("👑 Welcome to 240 Chess. Type help to get started. 👑\n");

        while (true) {
            System.out.print((loggedIn ? "[User: " + currentAuth.username() + "] " : "[LOGGED_OUT]") + " >>> ");

            // Get user input
            String line = scanner.nextLine();
            String[] split_line = line.split(" ");

            // Create command values
            String cmd_name = split_line[0].toLowerCase();
            String[] cmd_args = Arrays.copyOfRange(split_line, 1, split_line.length);

            if (!loggedIn) {
                switch (cmd_name) {
                    case "help":
                        genericOutput("↪ AVAILABLE COMMANDS:");
                        genericOutput("↪  register <USERNAME> <PASSWORD> <EMAIL>");
                        genericOutput("↪  login <USERNAME> <PASSWORD>");
                        genericOutput("↪  help");
                        genericOutput("↪  quit");
                        break;
                    case "register":
                        if (forceNArgs(cmd_args, 3, "register", "<USERNAME> <PASSWORD> <EMAIL>")) {
                            break;
                        }

                        try {
                            UserData userData = new UserData(cmd_args[0], cmd_args[1], cmd_args[2]);
                            currentAuth = facade.register(userData);
                            if (currentAuth != null) {
                                loggedIn = true;
                                successOutput("↪  Successfully registered and logged in");
                            }
                        } catch (ConnectionException exception) {
                            failureOutput("↪  Failed to connect to the server");
                        } catch (BadRequestException exception) {
                            failureOutput("↪  Failed to register and login: malformed request");
                        } catch (GenericTakenException exception) {
                            failureOutput("↪  Failed to register and login: username already taken");
                        } catch (Exception exception) {
                            failureOutput("↪  Failed to login: unknown error");
                        }
                        break;
                    case "login":
                        if (forceNArgs(cmd_args, 2, "login", "<USERNAME> <PASSWORD>")) {
                            break;
                        }

                        try {
                            UserData userData = new UserData(cmd_args[0], cmd_args[1], null);
                            currentAuth = facade.login(userData);
                            if (currentAuth != null) {
                                loggedIn = true;
                                successOutput("↪  Successfully logged in");
                            }
                        } catch (ConnectionException exception) {
                            failureOutput("↪  Failed to connect to the server");
                        } catch (BadRequestException exception) {
                            failureOutput("↪  Failed to login: malformed request");
                        } catch (UnauthorizedException exception) {
                            failureOutput("↪  Failed to login: invalid username or password");
                        } catch (Exception exception) {
                            failureOutput("↪  Failed to login: unknown error");
                        }
                        break;
                    case "quit":
                        successOutput("↪ Thanks for playing!");
                        return;
                    default:
                        failureOutput("↪ Invalid Command!");
                }
            } else {
                switch (cmd_name) {
                    case "help":
                        genericOutput("↪ AVAILABLE COMMANDS:");
                        genericOutput("↪  create <NAME> - Create a game");
                        genericOutput("↪  list - Get a list of games");
                        genericOutput("↪  join <id> [WHITE|BLACK] - Play a game");
                        genericOutput("↪  observe <id> - Observe a game");
                        genericOutput("↪  logout");
                        genericOutput("↪  help");
                        genericOutput("↪  quit");
                        break;
                    case "create":
                        if (forceNArgs(cmd_args, 1, "create", "<GAME_NAME>")) {
                            break;
                        }

                        try {
                            ServerFacade.createGameResponse response = facade.createGame(currentAuth, cmd_args[0]);
                            successOutput("↪  successfully created game with id " + response.gameID() + "!");
                        } catch (ConnectionException exception) {
                            failureOutput("↪  Failed to connect to the server");
                        } catch (BadRequestException exception) {
                            failureOutput("↪  Failed to create game: malformed request");
                        } catch (GenericTakenException exception) {
                            failureOutput("↪  Failed to create game: game already taken");
                        } catch (GameNotFoundException exception) {
                            failureOutput("↪  Failed to create game: game not found");
                        } catch (Exception exception) {
                            failureOutput("↪  Failed to create game: unknown error");
                        }
                    case "list":
                        if (forceNArgs(cmd_args, 0, "list", "")) {
                            break;
                        }

                        try {
                            GameData[] games = facade.listGames(currentAuth);
                            for (GameData gameData : games) {
                                String white = gameData.whiteUsername() != null ? gameData.whiteUsername() : "[OPEN]";
                                String black = gameData.blackUsername() != null ? gameData.blackUsername() : "[OPEN]";
                                successOutput(
                                    "↪  Game " + gameData.gameID() + " (" + gameData.gameName() + "): " +
                                    SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + white + RESET_BG_COLOR + SET_TEXT_COLOR_GREEN + " (white), " +
                                    SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + black + RESET_BG_COLOR + SET_TEXT_COLOR_GREEN + " (black)"
                                );
                            }
                        } catch (ConnectionException exception) {
                            failureOutput("↪  Failed to connect to the server");
                        } catch (UnauthorizedException exception) {
                            failureOutput("↪  Failed to list games: unauthorized");
                        } catch (GenericTakenException | GameNotFoundException | BadRequestException e) {
                            failureOutput("↪  Failed to list games: unknown error");
                        }
                    case "join":
                        break;
                    case "observe":
                        break;
                    case "logout":
                        if (forceNArgs(cmd_args, 0, "logout", "")) {
                            break;
                        }

                        try {
                            facade.logout(currentAuth);
                            currentAuth = null;
                            loggedIn = false;
                            successOutput("↪  Successfully logged out");
                        } catch (ConnectionException exception) {
                            failureOutput("↪  Failed to connect to the server");
                        } catch (UnauthorizedException exception) {
                            failureOutput("↪  Failed to logout: unauthorized");
                        } catch (Exception exception) {
                            failureOutput("↪  Failed to logout: unknown error");
                        }
                        break;
                    case "quit":
                        successOutput("↪ Thanks for playing!");
                        return;
                    default:
                        failureOutput("↪ Invalid Command!");
                }
            }
        }
    }

    static void successOutput(String output) {
        System.out.println(SET_TEXT_COLOR_GREEN + output + RESET_TEXT_COLOR);
    }

    static void failureOutput(String output) {
        System.out.println(SET_TEXT_COLOR_RED + output + RESET_TEXT_COLOR);
    }

    static void genericOutput(String output) {
        System.out.println(output);
    }

    static boolean forceNArgs(String[] args, int count, String command, String argsDefinition) {
        if (args.length != count) {
            String delimiter = count > 0 ? ": " : "";
            failureOutput("↪  `" + command + "` requires " + count + " arguments" + delimiter + argsDefinition);
            return true; // break
        }
        return false; // continue
    }
}