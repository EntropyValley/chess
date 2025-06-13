package ui;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static java.lang.Integer.parseInt;
import static ui.EscapeSequences.*;

public class ClientUtils {
    public static void outputGame(GameData game, ChessGame.TeamColor color, ChessPosition highlightStart, HashSet<ChessPosition> highlightEnd) {
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
            int rawNumber = parseInt(trimmedNumber);

            System.out.print(SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK + number);

            for (int i=1; i<=8; i++) {
                String bgColor;
                ChessPosition position = new ChessPosition(parseInt(number), i);

                if (position.equals(highlightStart)) {
                    bgColor = SET_BG_COLOR_YELLOW;
                } else if (highlightEnd.contains(position)) {
                    bgColor = SET_BG_COLOR_GREEN;
                } else if (rawNumber%2==0) {
                    if (color == ChessGame.TeamColor.WHITE) {
                        bgColor = i%2==0 ? SET_BG_COLOR_BLACK : SET_BG_COLOR_WHITE;
                    } else {
                        bgColor = i%2==0 ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK;
                    }
                } else {
                    if (color == ChessGame.TeamColor.WHITE) {
                        bgColor = i%2==0 ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK;
                    } else {
                        bgColor = i%2==0 ? SET_BG_COLOR_BLACK : SET_BG_COLOR_WHITE;
                    }
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

    public static ArrayList<String> generateDefaultNumbersArray() {
        return new ArrayList<>(
                Arrays.asList(" 8 ", " 7 ", " 6 ", " 5 ", " 4 ", " 3 ", " 2 ", " 1 ")
        );
    }

    public static ArrayList<String> generateDefaultLettersArray() {
        return new ArrayList<>(
                Arrays.asList(" a ", " b ", " c ", " d ", " e ", " f ", " g ", " h ")
        );
    }

    public static void successOutput(String output) {
        System.out.println(SET_TEXT_COLOR_GREEN + output + RESET_TEXT_COLOR);
    }

    public static void failureOutput(String output) {
        System.out.println(SET_TEXT_COLOR_RED + output + RESET_TEXT_COLOR);
    }

    public static void genericOutput(String output) {
        System.out.println(output);
    }
}
