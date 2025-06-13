import chess.*;
import client.ServerFacade;
import exceptions.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import ui.ClientState;
import ui.ClientUtils;

import java.util.*;

import static ui.EscapeSequences.*;

public class Main {
    static ClientState currentState = ClientState.LOGGED_OUT;
    static AuthData currentAuth = null;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ServerFacade facade = new ServerFacade("http://localhost:8083");

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
            String[] splitLine = line.split(" ");

            // Create command values
            String cmdName = splitLine[0].toLowerCase();
            String[] cmdArgs = Arrays.copyOfRange(splitLine, 1, splitLine.length);

            switch (currentState) {
                case LOGGED_OUT:
                    if (handleLoggedOutCommands(cmdName, cmdArgs, facade)) {
                        return;
                    }
                    break;
                case LOGGED_IN:
                    if (handleLoggedInCommands(cmdName, cmdArgs, facade)) {
                        return;
                    }
                    break;
                case PLAYING: // Phase 6
                case OBSERVING: // Phase 6
                default:
                    break;
            }
        }
    }

    private static boolean handleLoggedInCommands(String cmdName, String[] cmdArgs, ServerFacade facade) {
        switch (cmdName) {
            case "help":
                ClientUtils.genericOutput("↪ AVAILABLE COMMANDS:");
                ClientUtils.genericOutput("↪  create <NAME> - Create a game");
                ClientUtils.genericOutput("↪  list - Get a list of games");
                ClientUtils.genericOutput("↪  play <id> [WHITE|BLACK] - Play a game");
                ClientUtils.genericOutput("↪  observe <id> - Observe a game");
                ClientUtils.genericOutput("↪  logout");
                ClientUtils.genericOutput("↪  help");
                ClientUtils.genericOutput("↪  quit");
                break;
            case "create":
                handleCreateCommand(cmdArgs, facade);
                break;
            case "list":
                if (forceNArgs(cmdArgs, 0, "list", "")) {
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
                        ClientUtils.successOutput(
                                "↪  Game " + increment + " (" + gameData.gameName() + "): " +
                                        SET_BG_COLOR_BLACK + SET_TEXT_COLOR_WHITE + white + RESET_BG_COLOR + SET_TEXT_COLOR_GREEN + " (white), " +
                                        SET_BG_COLOR_WHITE + SET_TEXT_COLOR_BLACK + black + RESET_BG_COLOR + SET_TEXT_COLOR_GREEN + " (black)"
                        );
                        increment += 1;
                    }
                } catch (ConnectionException exception) {
                    ClientUtils.failureOutput("↪  Failed to connect to the server");
                } catch (UnauthorizedException exception) {
                    ClientUtils.failureOutput("↪  Failed to list games: unauthorized");
                } catch (GenericTakenException | GameNotFoundException | BadRequestException e) {
                    ClientUtils.failureOutput("↪  Failed to list games: unknown error");
                }
                break;
            case "play":
                handlePlayCommand(cmdArgs, facade);
                break;
            case "observe":
                handleObserveCommand(cmdArgs, facade);
                break;
            case "logout":
                if (forceNArgs(cmdArgs, 0, "logout", "")) {
                    break;
                }

                try {
                    facade.logout(currentAuth);
                    currentAuth = null;
                    currentState = ClientState.LOGGED_OUT;
                    ClientUtils.successOutput("↪  Successfully logged out");
                } catch (ConnectionException exception) {
                    ClientUtils.failureOutput("↪  Failed to connect to the server");
                } catch (UnauthorizedException exception) {
                    ClientUtils.failureOutput("↪  Failed to logout: unauthorized");
                } catch (Exception exception) {
                    ClientUtils.failureOutput("↪  Failed to logout: unknown error");
                }
                break;
            case "quit":
                ClientUtils.successOutput("↪ Thanks for playing!");
                return true;
            case "deletealldataiunderstandtheconsequencesofthisaction":
                try {
                    facade.clear();
                    currentAuth = null;
                    currentState = ClientState.LOGGED_OUT;
                    ClientUtils.successOutput("↪  Successfully wiped all traces of society from this server");
                } catch (ConnectionException exception) {
                    ClientUtils.failureOutput("↪  Failed to connect to the server");
                } catch (Exception exception) {
                    ClientUtils.failureOutput("↪  Failed to completely erase all data from the server");
                }
                break;
            default:
                ClientUtils.failureOutput("↪ Invalid Command!");
                break;
        }

        return false;
    }

    private static void handleCreateCommand(String[] cmdArgs, ServerFacade facade) {
        if (forceNArgs(cmdArgs, 1, "create", "<GAME_NAME>")) {
            return;
        }

        try {
            ServerFacade.CreateGameResponse response = facade.createGame(currentAuth, cmdArgs[0]);
            if (response != null) {
                ClientUtils.successOutput("↪  successfully created game!");
            } else {
                ClientUtils.failureOutput("↪  Failed to create game: unknown error");
            }
        } catch (ConnectionException exception) {
            ClientUtils.failureOutput("↪  Failed to connect to the server");
        } catch (BadRequestException exception) {
            ClientUtils.failureOutput("↪  Failed to create game: malformed request");
        } catch (GenericTakenException exception) {
            ClientUtils.failureOutput("↪  Failed to create game: game already taken");
        } catch (GameNotFoundException exception) {
            ClientUtils.failureOutput("↪  Failed to create game: game not found");
        } catch (Exception exception) {
            ClientUtils.failureOutput("↪  Failed to create game: unknown error");
        }
    }

    record PlayerInfo(GameData gameData, ChessGame.TeamColor teamColor) {}

    private static void handleObserveCommand(String[] cmdArgs, ServerFacade facade) {
        if (forceNArgs(cmdArgs, 2, "observe", "<GAME_INDEX> <COLOR:WHITE|BLACK>")) {
            return;
        }

        PlayerInfo playerInfo = getMatchedGame(cmdArgs, facade);
        if (playerInfo == null) {
            return;
        }

        ClientUtils.outputGame(playerInfo.gameData(), playerInfo.teamColor());
    }

    private static PlayerInfo getMatchedGame(String[] cmdArgs, ServerFacade facade) {
        int observationIndex;

        try {
            observationIndex = Integer.parseInt(cmdArgs[0]);
        } catch (NumberFormatException e) {
            ClientUtils.failureOutput("↪  <GAME_INDEX> is not a number>");
            return null;
        }

        ChessGame.TeamColor observationColor;
        String observationColorArg = cmdArgs[1].toLowerCase();

        if (observationColorArg.equals("white")) {
            observationColor = ChessGame.TeamColor.WHITE;
        } else if (observationColorArg.equals("black")) {
            observationColor = ChessGame.TeamColor.BLACK;
        } else {
            ClientUtils.failureOutput("↪  <COLOR> is not WHITE or BLACK");
            return null;
        }

        List<GameData> observableGameList;

        try {
            GameData[] games = facade.listGames(currentAuth);
            observableGameList = new ArrayList<>(Arrays.asList(games));

            sortGameList(observableGameList);
        } catch (UnauthorizedException exception) {
            ClientUtils.failureOutput("↪  Failed to fetch games: unauthorized");
            return null;
        } catch (Exception exception) {
            ClientUtils.failureOutput("↪  Failed to fetch games");
            return null;
        }

        GameData observedGame;

        try {
            observedGame = observableGameList.get(observationIndex - 1);
        } catch (Exception e) {
            ClientUtils.failureOutput("↪  Game " + observationIndex + " not available");
            return null;
        }

        return new PlayerInfo(observedGame, observationColor);
    }

    private static void handlePlayCommand(String[] cmdArgs, ServerFacade facade) {
        if (forceNArgs(cmdArgs, 2, "play", "<GAME_INDEX> <COLOR:WHITE|BLACK>")) {
            return;
        }

        PlayerInfo playerInfo = getMatchedGame(cmdArgs, facade);

        if (playerInfo == null) {
            return;
        }

        boolean skipJoin = false;

        if (playerInfo.teamColor() == ChessGame.TeamColor.WHITE && playerInfo.gameData().whiteUsername() != null) {
            if (!playerInfo.gameData().whiteUsername().equals(currentAuth.username())) {
                ClientUtils.failureOutput("↪  Color WHITE not available for this game");
                return;
            } else {
                skipJoin = true;
            }
        } else if (playerInfo.teamColor() == ChessGame.TeamColor.BLACK && playerInfo.gameData().blackUsername() != null) {
            if (!playerInfo.gameData().blackUsername().equals(currentAuth.username())) {
                ClientUtils.failureOutput("↪  Color BLACK not available for this game");
                return;
            } else {
                skipJoin = true;
            }
        }

        if (!skipJoin) {
            try {
                facade.joinGame(currentAuth, playerInfo.gameData().gameID(), playerInfo.teamColor().toString().toLowerCase());
            } catch (ConnectionException exception) {
                ClientUtils.failureOutput("↪  Failed to connect to the server");
                return;
            } catch (BadRequestException exception) {
                ClientUtils.failureOutput("↪  Failed to join game: malformed request");
                return;
            } catch (UnauthorizedException exception) {
                ClientUtils.failureOutput("↪  Failed to join game: unauthorized");
                return;
            } catch (GenericTakenException exception) {
                ClientUtils.failureOutput("↪  Failed to join game: color already taken");
                return;
            } catch (GameNotFoundException exception) {
                ClientUtils.failureOutput("↪  Failed to join game: game not found");
                return;
            }
        } else {
            ClientUtils.successOutput("↪  Rejoining game...");
        }

        ClientUtils.outputGame(playerInfo.gameData(), playerInfo.teamColor());
    }

    private static void sortGameList(List<GameData> gameList) {
        gameList.sort((o1, o2) -> {
            if (o1.gameID() == o2.gameID()) {
                return 0;
            }
            return o1.gameID() > o2.gameID() ? 1 : -1;
        });
    }

    private static boolean handleLoggedOutCommands(String cmdName, String[] cmdArgs, ServerFacade facade) {
        switch (cmdName) {
            case "help":
                ClientUtils.genericOutput("↪ AVAILABLE COMMANDS:");
                ClientUtils.genericOutput("↪  register <USERNAME> <PASSWORD> <EMAIL>");
                ClientUtils.genericOutput("↪  login <USERNAME> <PASSWORD>");
                ClientUtils. genericOutput("↪  help");
                ClientUtils.genericOutput("↪  quit");
                break;
            case "register":
                if (forceNArgs(cmdArgs, 3, "register", "<USERNAME> <PASSWORD> <EMAIL>")) {
                    break;
                }

                try {
                    UserData userData = new UserData(cmdArgs[0], cmdArgs[1], cmdArgs[2]);
                    currentAuth = facade.register(userData);
                    if (currentAuth != null) {
                        currentState = ClientState.LOGGED_IN;
                        ClientUtils.successOutput("↪  Successfully registered and logged in");
                    }
                } catch (ConnectionException exception) {
                    ClientUtils.failureOutput("↪  Failed to connect to the server");
                } catch (BadRequestException exception) {
                    ClientUtils.failureOutput("↪  Failed to register and login: malformed request");
                } catch (GenericTakenException exception) {
                    ClientUtils.failureOutput("↪  Failed to register and login: username already taken");
                } catch (Exception exception) {
                    ClientUtils.failureOutput("↪  Failed to login: unknown error");
                }
                break;
            case "login":
                if (forceNArgs(cmdArgs, 2, "login", "<USERNAME> <PASSWORD>")) {
                    break;
                }

                try {
                    UserData userData = new UserData(cmdArgs[0], cmdArgs[1], null);
                    currentAuth = facade.login(userData);
                    if (currentAuth != null) {
                        currentState = ClientState.LOGGED_IN;
                        ClientUtils.successOutput("↪  Successfully logged in");
                    }
                } catch (ConnectionException exception) {
                    ClientUtils.failureOutput("↪  Failed to connect to the server");
                } catch (BadRequestException exception) {
                    ClientUtils.failureOutput("↪  Failed to login: malformed request");
                } catch (UnauthorizedException exception) {
                    ClientUtils.failureOutput("↪  Failed to login: invalid username or password");
                } catch (Exception exception) {
                    ClientUtils.failureOutput("↪  Failed to login: unknown error");
                }
                break;
            case "quit":
                ClientUtils.successOutput("↪ Thanks for playing!");
                return true;
            default:
                ClientUtils.failureOutput("↪ Invalid Command!");
                break;
        }
        return false;
    }

    static boolean forceNArgs(String[] args, int count, String command, String argsDefinition) {
        if (args.length != count) {
            String delimiter = count > 0 ? ": " : "";
            ClientUtils.failureOutput("↪  `" + command + "` requires " + count + " arguments" + delimiter + argsDefinition);
            return true; // break
        }
        return false; // continue
    }
}