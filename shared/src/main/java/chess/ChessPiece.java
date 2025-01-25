package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor color;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.color = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        HashSet<ChessMove> moves = new HashSet<>();

        switch (type) {
            case KING:
                ChessPosition kingUp = getDirectedPositions(myPosition, Direction.UP, 1)[0];
                getKingMove(board, myPosition, moves, kingUp);

                ChessPosition kingUpRight = getDirectedPositions(myPosition, Direction.UP_RIGHT, 1)[0];
                getKingMove(board, myPosition, moves, kingUpRight);

                ChessPosition kingRight = getDirectedPositions(myPosition, Direction.RIGHT, 1)[0];
                getKingMove(board, myPosition, moves, kingRight);

                ChessPosition kingDownRight = getDirectedPositions(myPosition, Direction.DOWN_RIGHT, 1)[0];
                getKingMove(board, myPosition, moves, kingDownRight);

                ChessPosition kingDown = getDirectedPositions(myPosition, Direction.DOWN, 1)[0];
                getKingMove(board, myPosition, moves, kingDown);

                ChessPosition kingDownLeft = getDirectedPositions(myPosition, Direction.DOWN_LEFT, 1)[0];
                getKingMove(board, myPosition, moves, kingDownLeft);

                ChessPosition kingLeft = getDirectedPositions(myPosition, Direction.LEFT, 1)[0];
                getKingMove(board, myPosition, moves, kingLeft);

                ChessPosition kingUpLeft = getDirectedPositions(myPosition, Direction.UP_LEFT, 1)[0];
                getKingMove(board, myPosition, moves, kingUpLeft);
                break;
            case QUEEN:
                ChessPosition[] queenUpPositions = getDirectedPositions(myPosition, Direction.UP, 8);
                generateMovesFromPositions(board, myPosition, moves, queenUpPositions);

                ChessPosition[] queenUpRightPositions = getDirectedPositions(myPosition, Direction.UP_RIGHT, 8);
                generateMovesFromPositions(board, myPosition, moves, queenUpRightPositions);

                ChessPosition[] queenRightPositions = getDirectedPositions(myPosition, Direction.RIGHT, 8);
                generateMovesFromPositions(board, myPosition, moves, queenRightPositions);

                ChessPosition[] queenDownRightPositions = getDirectedPositions(myPosition, Direction.DOWN_RIGHT, 8);
                generateMovesFromPositions(board, myPosition, moves, queenDownRightPositions);

                ChessPosition[] queenDownPositions = getDirectedPositions(myPosition, Direction.DOWN, 8);
                generateMovesFromPositions(board, myPosition, moves, queenDownPositions);

                ChessPosition[] queenDownLeftPositions = getDirectedPositions(myPosition, Direction.DOWN_LEFT, 8);
                generateMovesFromPositions(board, myPosition, moves, queenDownLeftPositions);

                ChessPosition[] queenLeftPositions = getDirectedPositions(myPosition, Direction.LEFT, 8);
                generateMovesFromPositions(board, myPosition, moves, queenLeftPositions);

                ChessPosition[] queenUpLeftPositions = getDirectedPositions(myPosition, Direction.UP_LEFT, 8);
                generateMovesFromPositions(board, myPosition, moves, queenUpLeftPositions);
                break;
            case BISHOP:
                ChessPosition[] bishopUpRightPositions = getDirectedPositions(myPosition, Direction.UP_RIGHT, 8);
                generateMovesFromPositions(board, myPosition, moves, bishopUpRightPositions);

                ChessPosition[] bishopDownRightPositions = getDirectedPositions(myPosition, Direction.DOWN_RIGHT, 8);
                generateMovesFromPositions(board, myPosition, moves, bishopDownRightPositions);

                ChessPosition[] bishopDownLeftPositions = getDirectedPositions(myPosition, Direction.DOWN_LEFT, 8);
                generateMovesFromPositions(board, myPosition, moves, bishopDownLeftPositions);

                ChessPosition[] bishopUpLeftPositions = getDirectedPositions(myPosition, Direction.UP_LEFT, 8);
                generateMovesFromPositions(board, myPosition, moves, bishopUpLeftPositions);
                break;
            case KNIGHT:
                if (myPosition.getRow() + 2 <= 8 && myPosition.getColumn() + 1 <= 8) {
                    ChessPosition tallerUpRight = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() + 1);
                    ChessPiece knightTallerUpRightTarget = board.getPiece(tallerUpRight);

                    if (knightTallerUpRightTarget != null) {
                        if (knightTallerUpRightTarget.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, tallerUpRight, null));
                        }
                    } else {
                        moves.add(new ChessMove(myPosition, tallerUpRight, null));
                    }
                }

                if (myPosition.getRow() + 1 <= 8 && myPosition.getColumn() + 2 <= 8) {
                    ChessPosition shorterUpRight = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 2);
                    ChessPiece shorterUpRightTarget = board.getPiece(shorterUpRight);

                    if (shorterUpRightTarget != null) {
                        if (shorterUpRightTarget.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, shorterUpRight, null));
                        }
                    } else {
                        moves.add(new ChessMove(myPosition, shorterUpRight, null));
                    }
                }

                if (myPosition.getRow() - 1 >= 1 && myPosition.getColumn() + 2 <= 8) {
                    ChessPosition shorterDownRight = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 2);
                    ChessPiece shorterDownRightTarget = board.getPiece(shorterDownRight);

                    if (shorterDownRightTarget != null) {
                        if (shorterDownRightTarget.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, shorterDownRight, null));
                        }
                    } else {
                        moves.add(new ChessMove(myPosition, shorterDownRight, null));
                    }
                }

                if (myPosition.getRow() - 2 >= 1 && myPosition.getColumn() + 1 <= 8) {
                    ChessPosition tallerDownRight = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() + 1);
                    ChessPiece tallerDownRightTarget = board.getPiece(tallerDownRight);

                    if (tallerDownRightTarget != null) {
                        if (tallerDownRightTarget.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, tallerDownRight, null));
                        }
                    } else {
                        moves.add(new ChessMove(myPosition, tallerDownRight, null));
                    }
                }

                if (myPosition.getRow() - 2 >= 1 && myPosition.getColumn() - 1 >= 1) {
                    ChessPosition tallerDownLeft = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn() - 1);
                    ChessPiece tallerDownLeftTarget = board.getPiece(tallerDownLeft);

                    if (tallerDownLeftTarget != null) {
                        if (tallerDownLeftTarget.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, tallerDownLeft, null));
                        }
                    } else {
                        moves.add(new ChessMove(myPosition, tallerDownLeft, null));
                    }
                }

                if (myPosition.getRow() - 1 >= 1 && myPosition.getColumn() - 2 >= 1) {
                    ChessPosition shorterDownLeft = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 2);
                    ChessPiece shorterDownLeftTarget = board.getPiece(shorterDownLeft);

                    if (shorterDownLeftTarget != null) {
                        if (shorterDownLeftTarget.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, shorterDownLeft, null));
                        }
                    } else {
                        moves.add(new ChessMove(myPosition, shorterDownLeft, null));
                    }
                }

                if (myPosition.getRow() + 1 <= 8 && myPosition.getColumn() - 2 >= 1) {
                    ChessPosition shorterUpLeft = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 2);
                    ChessPiece shorterUpLeftTarget = board.getPiece(shorterUpLeft);

                    if (shorterUpLeftTarget != null) {
                        if (shorterUpLeftTarget.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, shorterUpLeft, null));
                        }
                    } else {
                        moves.add(new ChessMove(myPosition, shorterUpLeft, null));
                    }
                }

                if (myPosition.getRow() + 2 <= 8 && myPosition.getColumn() - 1 >= 1) {
                    ChessPosition tallerUpLeft = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn() - 1);
                    ChessPiece tallerUpLeftTarget = board.getPiece(tallerUpLeft);

                    if (tallerUpLeftTarget != null) {
                        if (tallerUpLeftTarget.getTeamColor() != color) {
                            moves.add(new ChessMove(myPosition, tallerUpLeft, null));
                        }
                    } else {
                        moves.add(new ChessMove(myPosition, tallerUpLeft, null));
                    }
                }
                break;
            case ROOK:
                ChessPosition[] rookUpPositions = getDirectedPositions(myPosition, Direction.UP, 8);
                generateMovesFromPositions(board, myPosition, moves, rookUpPositions);

                ChessPosition[] rookRightPositions = getDirectedPositions(myPosition, Direction.RIGHT, 8);
                generateMovesFromPositions(board, myPosition, moves, rookRightPositions);

                ChessPosition[] rookDownPositions = getDirectedPositions(myPosition, Direction.DOWN, 8);
                generateMovesFromPositions(board, myPosition, moves, rookDownPositions);

                ChessPosition[] rookLeftPositions = getDirectedPositions(myPosition, Direction.LEFT, 8);
                generateMovesFromPositions(board, myPosition, moves, rookLeftPositions);
                break;
            case PAWN:
                switch(color) {
                    case WHITE:
                        if (myPosition.getRow() + 1 <= 8) {
                            ChessPosition whiteSingleMove = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
                            addSingleMoves(board, myPosition, moves, whiteSingleMove);
                        }

                        if (myPosition.getRow() + 1 <= 8 && myPosition.getColumn() - 1 >= 1) {
                            ChessPosition whiteCaptureLeft = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() - 1);
                            handlePawnCapture(board, myPosition, moves, whiteCaptureLeft);
                        }

                        if (myPosition.getRow() + 1 <= 8 && myPosition.getColumn() + 1 <= 8) {
                            ChessPosition whiteCaptureRight = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn() + 1);
                            handlePawnCapture(board, myPosition, moves, whiteCaptureRight);
                        }

                        if (myPosition.getRow() == 2) {
                            ChessPosition whiteBlockedPosition = new ChessPosition(myPosition.getRow() + 1, myPosition.getColumn());
                            ChessPosition whiteDoublePos = new ChessPosition(myPosition.getRow() + 2, myPosition.getColumn());

                            if (board.getPiece(whiteBlockedPosition) == null && board.getPiece(whiteDoublePos) == null) {
                                moves.add(new ChessMove(myPosition, whiteDoublePos, null));
                            }
                        }
                        break;
                    case BLACK:
                        if (myPosition.getRow() - 1 >= 1) {
                            ChessPosition blackSingleMove = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
                            addSingleMoves(board, myPosition, moves, blackSingleMove);
                        }

                        if (myPosition.getRow() - 1 >= 1 && myPosition.getColumn() - 1 >= 1) {
                            ChessPosition blackCaptureLeft = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() - 1);
                            handlePawnCapture(board, myPosition, moves, blackCaptureLeft);
                        }

                        if (myPosition.getRow() - 1 >= 1 && myPosition.getColumn() + 1 <= 8) {
                            ChessPosition blackCaptureRight = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn() + 1);
                            handlePawnCapture(board, myPosition, moves, blackCaptureRight);
                        }

                        if (myPosition.getRow() == 7) {
                            ChessPosition blackBlockedPosition = new ChessPosition(myPosition.getRow() - 1, myPosition.getColumn());
                            ChessPosition blackDoublePos = new ChessPosition(myPosition.getRow() - 2, myPosition.getColumn());

                            if (board.getPiece(blackBlockedPosition) == null && board.getPiece(blackDoublePos) == null) {
                                moves.add(new ChessMove(myPosition, blackDoublePos, null));
                            }
                        }
                        break;
                }

                break;
        }

        return moves;
    }

    private void handlePawnCapture(ChessBoard board, ChessPosition myPosition, HashSet<ChessMove> moves, ChessPosition capturePosition) {
        ChessPiece targetCapturePiece = board.getPiece(capturePosition);
        if (targetCapturePiece != null) {
            if (targetCapturePiece.getTeamColor() != color) {
                if (capturePosition.getRow() == 8 || capturePosition.getRow() == 1) {
                    moves.add(new ChessMove(myPosition, capturePosition, PieceType.QUEEN));
                    moves.add(new ChessMove(myPosition, capturePosition, PieceType.BISHOP));
                    moves.add(new ChessMove(myPosition, capturePosition, PieceType.KNIGHT));
                    moves.add(new ChessMove(myPosition, capturePosition, PieceType.ROOK));
                } else {
                    moves.add(new ChessMove(myPosition, capturePosition, null));
                }
            }
        }
    }

    private void getKingMove(ChessBoard board, ChessPosition myPosition, HashSet<ChessMove> moves, ChessPosition position) {
        if (position != null) {
            ChessPiece kingUpRightTarget = board.getPiece(position);

            if (kingUpRightTarget != null) {
                if (kingUpRightTarget.getTeamColor() != color) {
                    moves.add(new ChessMove(myPosition, position, null));
                }
            } else {
                moves.add(new ChessMove(myPosition, position, null));
            }
        }
    }

    private void generateMovesFromPositions(ChessBoard board, ChessPosition myPosition, HashSet<ChessMove> moves, ChessPosition[] targetPositions) {
        for (int i=0; i<8; i++) {
            if (targetPositions[i] == null) {
                break;
            }

            ChessPiece queenTarget = board.getPiece(targetPositions[i]);

            if (queenTarget == null) {
                moves.add(new ChessMove(myPosition, targetPositions[i], null));
            } else {
                if (queenTarget.getTeamColor() != color) {
                    moves.add(new ChessMove(myPosition, targetPositions[i], null));
                }
                break;
            }
        }
    }

    private void addSingleMoves(ChessBoard board, ChessPosition myPosition, HashSet<ChessMove> moves, ChessPosition singleMoveTarget) {
        if (board.getPiece(singleMoveTarget) == null) {
            if (singleMoveTarget.getRow() == 8 || singleMoveTarget.getRow() == 1) {
                moves.add(new ChessMove(myPosition, singleMoveTarget, PieceType.QUEEN));
                moves.add(new ChessMove(myPosition, singleMoveTarget, PieceType.BISHOP));
                moves.add(new ChessMove(myPosition, singleMoveTarget, PieceType.KNIGHT));
                moves.add(new ChessMove(myPosition, singleMoveTarget, PieceType.ROOK));
            } else {
                moves.add(new ChessMove(myPosition, singleMoveTarget, null));
            }
        }
    }

    private enum Direction {
        UP,
        UP_RIGHT,
        RIGHT,
        DOWN_RIGHT,
        DOWN,
        DOWN_LEFT,
        LEFT,
        UP_LEFT
    }

    private ChessPosition[] getDirectedPositions(ChessPosition currentPosition, Direction targetDir, int maxDistance) {
        ChessPosition[] positions = new ChessPosition[maxDistance];

        switch (targetDir) {
            case UP:
                for (int i = 1; i <= maxDistance; i++) {
                    if (currentPosition.getColumn() + i <= 8) {
                        positions[i-1] = new ChessPosition( currentPosition.getRow(), currentPosition.getColumn() + i);
                    } else {
                        positions[i-1] = null;
                    }
                }
                break;
            case UP_RIGHT:
                for (int i = 1; i <= maxDistance; i++) {
                    if (currentPosition.getColumn() + i <= 8 && currentPosition.getRow() + i <= 8) {
                        positions[i-1] = new ChessPosition(currentPosition.getRow() + i, currentPosition.getColumn() + i);
                    } else {
                        positions[i-1] = null;
                    }
                }
                break;
            case RIGHT:
                for (int i = 1; i <= maxDistance; i++) {
                    if (currentPosition.getRow() + i <= 8) {
                        positions[i-1] = new ChessPosition(currentPosition.getRow() + i, currentPosition.getColumn());
                    } else {
                        positions[i-1] = null;
                    }
                }
                break;
            case DOWN_RIGHT:
                for (int i = 1; i <= maxDistance; i++) {
                    if (currentPosition.getColumn() - i >= 1 && currentPosition.getRow() + i <= 8) {
                        positions[i-1] = new ChessPosition(currentPosition.getRow() + i, currentPosition.getColumn() - i);
                    } else {
                        positions[i-1] = null;
                    }
                }
                break;
            case DOWN:
                for (int i = 1; i <= maxDistance; i++) {
                    if (currentPosition.getColumn() - i >= 1) {
                        positions[i-1] = new ChessPosition(currentPosition.getRow(), currentPosition.getColumn() - i);
                    }
                }
                break;
            case DOWN_LEFT:
                for (int i = 1; i <= maxDistance; i++) {
                    if (currentPosition.getColumn() - i >= 1 && currentPosition.getRow() - i >= 1) {
                        positions[i-1] = new ChessPosition(currentPosition.getRow() - i, currentPosition.getColumn() - i);
                    } else {
                        positions[i-1] = null;
                    }
                }
                break;
            case LEFT:
                for (int i = 1; i <= maxDistance; i++) {
                    if (currentPosition.getRow() - i >= 1) {
                        positions[i-1] = new ChessPosition(currentPosition.getRow() - i, currentPosition.getColumn());
                    } else {
                        positions[i-1] = null;
                    }
                }
                break;
            case UP_LEFT:
                for (int i = 1; i <= maxDistance; i++) {
                    if (currentPosition.getColumn() + i <= 8 && currentPosition.getRow() - i >= 1) {
                        positions[i-1] = new ChessPosition(currentPosition.getRow() - i, currentPosition.getColumn() + i);
                    } else {
                        positions[i-1] = null;
                    }
                }
                break;
        }

        return positions;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        } else if (this == o) {
            return true;
        }

        ChessPiece that = (ChessPiece) o;
        return color == that.color && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, type);
    }

    @Override
    public String toString() {
        return "CHESSPIECE(" + color + " " + type + ")";
    }
}
