package chess;

import java.util.Collection;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor currentTurn;
    private ChessBoard currentBoard;

    public ChessGame() {
        // Set up initial Board
        this.currentBoard = new ChessBoard();
        currentBoard.resetBoard();

        // Starting Team = WHITE
        setTeamTurn(TeamColor.WHITE);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;

        // Find King
        for (int row=1; row<=8; row++) {
            for (int col=1; col<=8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = currentBoard.getPiece(currentPosition);
                if (currentPiece != null) {
                    if (currentPiece.getTeamColor() == teamColor &&
                        currentPiece.getPieceType() == ChessPiece.PieceType.KING) {
                        kingPosition = currentPosition;
                    }
                }
            }
        }

        // Check for found king
        if (kingPosition == null) {
            return false;
        }

        // Generate final version of kingPosition for use in matching moves
        final ChessPosition staticKingPosition = kingPosition;

        // Check for potential attacks
        for (int row=1; row<=8; row++) {
            for (int col=1; col<8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = currentBoard.getPiece(currentPosition);
                if (currentPiece != null) {
                    if (currentPiece.getTeamColor() != teamColor) {
                        Collection<ChessMove> opponentMoves = currentPiece.pieceMoves(currentBoard, currentPosition);
                        if (opponentMoves.stream().anyMatch(
                                move -> move.getEndPosition() == staticKingPosition)
                        ) {
                            return true;
                        }
                    }
                }
            }
        }

        // No check move found; not in check
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        // Assume no valid moves
        boolean isInStalemate = true;

        // Check for possible moves
        for (int row=1; row<=8; row++) {
            for (int col=1; col<=8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = currentBoard.getPiece(currentPosition);
                if (currentPiece != null) {
                    if (currentPiece.getTeamColor() == teamColor) {
                        Collection<ChessMove> pieceMoves = currentPiece.pieceMoves(currentBoard, currentPosition);
                        if (!pieceMoves.isEmpty()) {
                            isInStalemate = false;
                            break;
                        }
                    }
                }
            }
        }

        // Return whether valid moves were found
        return isInStalemate;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.currentBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return currentBoard;
    }
}
