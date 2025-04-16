import chess.*;
import client.ServerFacade;
import exceptions.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import ui.ClientState;

import java.util.*;

import static ui.EscapeSequences.*;

public class Main {
    static ClientState currentState = ClientState.LOGGED_OUT;
    static AuthData currentAuth = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ServerFacade facade = new ServerFacade("http://localhost:8081");

        System.out.print(ERASE_SCREEN);
        System.out.println("👑 Welcome to 240 Chess. Type help to get started. 👑\n");

        while (true) {
            switch (currentState) {
                case LOGGED_OUT:
                    System.out.print("[LOGGED_OUT] >>> ");
                    break;
                case LOGGED_IN:
                    System.out.print("[USER: " + currentAuth.username() + "] >>> ");
                    break;
                default:
                    break;
            }

            // Get user input
            String line = scanner.nextLine();
            String[] split_line = line.split(" ");

            // Create command values
            String cmd_name = split_line[0].toLowerCase();
            String[] cmd_args = Arrays.copyOfRange(split_line, 1, split_line.length);

            switch (currentState) {
                case LOGGED_OUT:
                    if (handleLoggedOutCommands(cmd_name, cmd_args, facade)) {
                        return;
                    }
                    break;
                case LOGGED_IN:
                    if (handleLoggedInCommands(cmd_name, cmd_args, facade)) {
                        return;
                    }
                    break;
                case PLAYING:
                    break;
                case OBSERVING:
                    break;
                default:
                    break;
            }
        }
    }

    private static boolean handleLoggedInCommands(String cmd_name, String[] cmd_args, ServerFacade facade) {
        switch (cmd_name) {
            case "help":
                genericOutput("↪ AVAILABLE COMMANDS:");
                genericOutput("↪  create <NAME> - Create a game");
                genericOutput("↪  list - Get a list of games");
                genericOutput("↪  play <id> [WHITE|BLACK] - Play a game");
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
                    if (response != null) {
                        successOutput("↪  successfully created game!");
                    } else {
                        failureOutput("↪  Failed to create game: unknown error");
                    }
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
                break;
            case "list":
                if (forceNArgs(cmd_args, 0, "list", "")) {
                    break;
                }

                try {
                    GameData[] games = facade.listGames(currentAuth);
                    List<GameData> gameList = new ArrayList<>(Arrays.asList(games));
                    sortGameList(gameList);

                    int increment = 1;
                    for (GameData gameData : gameList) {
                        String white = gameData.whiteUsername() != null ? gameData.whiteUsername() : "[OPEN]";
                        String black = gameData.blackUsername() != null ? gameData.blackUsername() : "[OPEN]";
                        successOutput(
                                "↪  Game " + increment + " (" + gameData.gameName() + "): " +
                                        SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + white + RESET_BG_COLOR + SET_TEXT_COLOR_GREEN + " (white), " +
                                        SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + black + RESET_BG_COLOR + SET_TEXT_COLOR_GREEN + " (black)"
                        );
                        increment += 1;
                    }
                } catch (ConnectionException exception) {
                    failureOutput("↪  Failed to connect to the server");
                } catch (UnauthorizedException exception) {
                    failureOutput("↪  Failed to list games: unauthorized");
                } catch (GenericTakenException | GameNotFoundException | BadRequestException e) {
                    failureOutput("↪  Failed to list games: unknown error");
                }
                break;
            case "play":
                if (forceNArgs(cmd_args, 2, "play", "<GAME_INDEX> <COLOR:WHITE|BLACK>")) {
                    break;
                }

                int index;

                try {
                    index = Integer.parseInt(cmd_args[0]);
                } catch (NumberFormatException e) {
                    failureOutput("↪  <GAME_INDEX> is not a number>");
                    break;
                }

                ChessGame.TeamColor color;
                String loweredColorArg = cmd_args[1].toLowerCase();

                if (loweredColorArg.equals("white")) {
                    color = ChessGame.TeamColor.WHITE;
                } else if (loweredColorArg.equals("black")) {
                    color = ChessGame.TeamColor.BLACK;
                } else {
                    failureOutput("↪  <COLOR> is not WHITE or BLACK");
                    break;
                }

                List<GameData> gameList;

                try {
                    GameData[] games = facade.listGames(currentAuth);
                    gameList = new ArrayList<>(Arrays.asList(games));

                    sortGameList(gameList);
                } catch (UnauthorizedException exception) {
                    failureOutput("↪  Failed to fetch games: unauthorized");
                    break;
                } catch (Exception exception) {
                    failureOutput("↪  Failed to fetch games");
                    break;
                }

                GameData game;

                try {
                    game = gameList.get(index - 1);
                } catch (Exception e) {
                    failureOutput("↪  Game " + index + " not available");
                    break;
                }

                boolean skipJoin = false;

                if (color == ChessGame.TeamColor.WHITE && game.whiteUsername() != null) {
                    if (!game.whiteUsername().equals(currentAuth.username())) {
                        failureOutput("↪  Color WHITE not available for this game");
                        break;
                    } else {
                        skipJoin = true;
                    }
                } else if (color == ChessGame.TeamColor.BLACK && game.blackUsername() != null) {
                    if (!game.blackUsername().equals(currentAuth.username())) {
                        failureOutput("↪  Color BLACK not available for this game");
                        break;
                    } else {
                        skipJoin = true;
                    }
                }

                if (!skipJoin) {
                    try {
                        facade.joinGame(currentAuth, game.gameID(), color.toString().toLowerCase());
                    } catch (ConnectionException exception) {
                        failureOutput("↪  Failed to connect to the server");
                        break;
                    } catch (BadRequestException exception) {
                        failureOutput("↪  Failed to join game: malformed request");
                        break;
                    } catch (UnauthorizedException exception) {
                        failureOutput("↪  Failed to join game: unauthorized");
                        break;
                    } catch (GenericTakenException exception) {
                        failureOutput("↪  Failed to join game: color already taken");
                        break;
                    } catch (GameNotFoundException exception) {
                        failureOutput("↪  Failed to join game: game not found");
                        break;
                    }
                } else {
                    successOutput("↪  Rejoining game...");
                }

                outputGame(game, color);
                break;
            case "observe":
                if (forceNArgs(cmd_args, 2, "observe", "<GAME_INDEX> <COLOR:WHITE|BLACK>")) {
                    break;
                }

                int observationIndex;

                try {
                    observationIndex = Integer.parseInt(cmd_args[0]);
                } catch (NumberFormatException e) {
                    failureOutput("↪  <GAME_INDEX> is not a number>");
                    break;
                }

                ChessGame.TeamColor observationColor;
                String observationColorArg = cmd_args[1].toLowerCase();

                if (observationColorArg.equals("white")) {
                    observationColor = ChessGame.TeamColor.WHITE;
                } else if (observationColorArg.equals("black")) {
                    observationColor = ChessGame.TeamColor.BLACK;
                } else {
                    failureOutput("↪  <COLOR> is not WHITE or BLACK");
                    break;
                }

                List<GameData> observableGameList;

                try {
                    GameData[] games = facade.listGames(currentAuth);
                    observableGameList = new ArrayList<>(Arrays.asList(games));

                    sortGameList(observableGameList);
                } catch (UnauthorizedException exception) {
                    failureOutput("↪  Failed to fetch games: unauthorized");
                    break;
                } catch (Exception exception) {
                    failureOutput("↪  Failed to fetch games");
                    break;
                }

                GameData observedGame;

                try {
                    observedGame = observableGameList.get(observationIndex - 1);
                } catch (Exception e) {
                    failureOutput("↪  Game " + observationIndex + " not available");
                    break;
                }

                outputGame(observedGame, observationColor);
                break;
            case "logout":
                if (forceNArgs(cmd_args, 0, "logout", "")) {
                    break;
                }

                try {
                    facade.logout(currentAuth);
                    currentAuth = null;
                    currentState = ClientState.LOGGED_OUT;
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
                return true;
            default:
                failureOutput("↪ Invalid Command!");
                break;
        }

        return false;
    }

    private static void outputGame(GameData game, ChessGame.TeamColor color) {
        boolean reverse = color == ChessGame.TeamColor.BLACK;

        ArrayList<String> letters = generateDefaultLettersArray();

        ArrayList<String> numbers = generateDefaultNumbersArray();

        if (reverse) {
            Collections.reverse(letters);
            Collections.reverse(numbers);
        }

        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + "   ");

        for (String letter : letters) {
            System.out.print(letter);
        }

        System.out.print("   " + RESET_BG_COLOR + "\n");

        ChessGame board = game.game();

        for (String number : numbers) {
            String trimmedNumber = number.trim();
            int rawNumber = Integer.parseInt(trimmedNumber);

            System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + number);

            for (int i=1; i<=8; i++) {
                String bgColor;
                if (rawNumber%2==0) {
                    bgColor = i%2==0 ? SET_BG_COLOR_BLACK : SET_BG_COLOR_WHITE;
                } else {
                    bgColor = i%2==0 ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK;
                }

                ChessPiece piece;

                if (reverse) {
                    piece = board.getBoard().getPiece(new ChessPosition(rawNumber, 9-i));
                } else {
                    piece = board.getBoard().getPiece(new ChessPosition(rawNumber, i));
                }

                if (piece != null) {
                    String textColor = piece.getTeamColor() == ChessGame.TeamColor.BLACK ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_GREEN;

                    switch(piece.getPieceType()) {
                        case PAWN:
                            System.out.print(bgColor + textColor + " P " + RESET_BG_COLOR );
                            break;
                        case QUEEN:
                            System.out.print(bgColor + textColor + " Q " + RESET_BG_COLOR );
                            break;
                        case KING:
                            System.out.print(bgColor + textColor + " K " + RESET_BG_COLOR );
                            break;
                        case KNIGHT:
                            System.out.print(bgColor + textColor + " N " + RESET_BG_COLOR );
                            break;
                        case ROOK:
                            System.out.print(bgColor + textColor + " R " + RESET_BG_COLOR );
                            break;
                        case BISHOP:
                            System.out.print(bgColor + textColor + " B " + RESET_BG_COLOR );
                            break;
                        default:
                            System.out.print(bgColor + textColor + "   " + RESET_BG_COLOR );
                            break;
                    }
                } else {
                    System.out.print(bgColor + "   " + RESET_BG_COLOR );
                }
            }

            System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + number);
            System.out.print(RESET_BG_COLOR + "\n");
        }

        System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + "   ");

        for (String letter : letters) {
            System.out.print(letter);
        }

        System.out.print("   " + RESET_BG_COLOR + RESET_TEXT_COLOR + "\n");

    }

    private static ArrayList<String> generateDefaultNumbersArray() {
        ArrayList<String> numbers = new ArrayList<>();
        numbers.add(" 8 ");
        numbers.add(" 7 ");
        numbers.add(" 6 ");
        numbers.add(" 5 ");
        numbers.add(" 4 ");
        numbers.add(" 3 ");
        numbers.add(" 2 ");
        numbers.add(" 1 ");
        return numbers;
    }

    private static ArrayList<String> generateDefaultLettersArray() {
        ArrayList<String> letters = new ArrayList<>();
        letters.add(" a ");
        letters.add(" b ");
        letters.add(" c ");
        letters.add(" d ");
        letters.add(" e ");
        letters.add(" f ");
        letters.add(" g ");
        letters.add(" h ");
        return letters;
    }

    private static void sortGameList(List<GameData> gameList) {
        gameList.sort((o1, o2) -> {
            if (o1.gameID() == o2.gameID()) {
                return 0;
            }
            return o1.gameID() > o2.gameID() ? 1 : -1;
        });
    }

    private static boolean handleLoggedOutCommands(String cmd_name, String[] cmd_args, ServerFacade facade) {
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
                        currentState = ClientState.LOGGED_IN;
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
                        currentState = ClientState.LOGGED_IN;
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
                return true;
            default:
                failureOutput("↪ Invalid Command!");
                break;
        }

        return false;
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