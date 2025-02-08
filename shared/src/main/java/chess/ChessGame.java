package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

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
        ChessPiece pieceAtPosition = currentBoard.getPiece(startPosition);
        return getValidPositionMovesForBoard(currentBoard, pieceAtPosition.getTeamColor(), startPosition);
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece.PieceType promotionPieceType = move.getPromotionPiece();
        ChessPiece existingPiece = currentBoard.getPiece(startPosition);

        // No piece; can't be moved
        if (existingPiece == null) {
            throw new InvalidMoveException("No Piece to move at " + startPosition);
        }

        TeamColor movingColor = existingPiece.getTeamColor();

        // Not the current turn; can't move a piece
        if (movingColor != currentTurn) {
            throw new InvalidMoveException("Not " + movingColor + "'s turn");
        }

        Collection<ChessMove> validPieceMoves = validMoves(startPosition);

        // If move is found amidst valid moves, make the move;  Else, the move cannot be performed
        if (validPieceMoves.contains(move)) {
            currentBoard.addPiece(startPosition, null);
            currentBoard.addPiece(
                endPosition,
                promotionPieceType != null ? new ChessPiece(movingColor, promotionPieceType) : existingPiece
            );
        } else {
            throw new InvalidMoveException("Invalid Move:" + move);
        }

        // Set the turn to the next team
        currentTurn = movingColor == TeamColor.BLACK ? TeamColor.WHITE : TeamColor.BLACK;
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return arbitraryBoardCheck(teamColor, currentBoard); // Check current board for check
    }

    // Check to see if a provided team is in check on a provided board
    private boolean arbitraryBoardCheck(TeamColor teamColor, ChessBoard boardToCheck) {
        final ChessPosition kingPosition = findKingPosition(teamColor, boardToCheck);

        // Check if king was found
        if (kingPosition == null) {
            return false;
        }

        // Check for potential attacks
        for (int row=1; row<=8; row++) {
            for (int col=1; col<8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = boardToCheck.getPiece(currentPosition);
                if (currentPiece != null) {
                    if (currentPiece.getTeamColor() != teamColor) {
                        Collection<ChessMove> opponentMoves = currentPiece.pieceMoves(boardToCheck, currentPosition);
                        if (opponentMoves.stream().anyMatch(
                                move -> move.getEndPosition().equals(kingPosition)
                            )
                        ) {
                            return true;
                        }
                    }
                }
            }
        }

        // No opponent move attacks king - not in check
        return false;
    }

    // Find the position of the king on a given board
    private static ChessPosition findKingPosition(TeamColor teamColor, ChessBoard boardToCheck) {
        ChessPosition kingPosition = null;

        // Iterate over board to find king
        for (int row=1; row<=8; row++) {
            for (int col=1; col<=8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece currentPiece = boardToCheck.getPiece(currentPosition);
                if (currentPiece != null) {
                    if (currentPiece.getTeamColor() == teamColor &&
                            currentPiece.getPieceType() == ChessPiece.PieceType.KING) {
                        kingPosition = currentPosition;
                    }
                }
            }
        }

        return kingPosition;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return false;
        }

        // If there are no moves, in checkmate
        return getValidMovesForBoard(currentBoard, teamColor).isEmpty();
    }

    // Get all possible moves for a team on a given board (potential or current)
    private HashSet<ChessMove> getValidMovesForBoard(ChessBoard boardToSearch, TeamColor teamColor) {
        // List of potential moves
        HashSet<ChessMove> validMoves = new HashSet<>();

        // Check for potential moves
        for (int row=1; row<=8; row++) {
            for (int col=1; col<8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                HashSet<ChessMove> validPieceMoves = getValidPositionMovesForBoard(boardToSearch, teamColor, currentPosition);
                validMoves.addAll(validPieceMoves);
            }
        }

        return validMoves;
    }

    // Get valid moves for a single position on an arbitrary board (either potential or current)
    private HashSet<ChessMove> getValidPositionMovesForBoard(ChessBoard boardToSearch, TeamColor teamColor, ChessPosition currentPosition) {
        ChessPiece currentPiece = boardToSearch.getPiece(currentPosition);
        HashSet<ChessMove> validPieceMoves = new HashSet<>();

        // If there's no current piece, there are no valid moves
        if (currentPiece != null) {

            // If the current team is trying to move a piece that isn't theirs, there are no valid moves
            if (currentPiece.getTeamColor() == teamColor) {
                // Fetch possible moves
                Collection<ChessMove> pieceMoves = currentPiece.pieceMoves(boardToSearch, currentPosition);

                // Iterate over these possible moves and create a potential board to see if it puts the team in check
                for (ChessMove move : pieceMoves) {
                    ChessBoard potentialBoard = createPotentialBoard(boardToSearch, move);
                    if (!arbitraryBoardCheck(teamColor, potentialBoard)) {
                        validPieceMoves.add(move); // If move doesn't put team in check, it's valid
                    }
                }
            }
        }

        return validPieceMoves;
    }

    // Create a board that represents a potential future for a provided board given a potential move
    private static ChessBoard createPotentialBoard(ChessBoard boardToSearch, ChessMove move) {
        // Create new board so that it can be changed and checked without affecting currentBoard
        ChessBoard potentialBoard = new ChessBoard();

        // Unpack move to avoid repetition
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();

        // Get piece and then generate resultant piece of move
        ChessPiece currentPiece = boardToSearch.getPiece(startPosition);
        ChessPiece resultantPiece = promotionPiece == null ?
            currentPiece :
            new ChessPiece(currentPiece.getTeamColor(), promotionPiece);

        // Iterate over the board; add move in place, add remaining pieces to board in existing locations
        for (int row=1; row<=8; row++) {
            for (int col=1; col<=8; col++) {
                ChessPosition currentPosition = new ChessPosition(row, col);
                ChessPiece searchedPiece = boardToSearch.getPiece(currentPosition);

                if (currentPosition.equals(endPosition)) {
                    potentialBoard.addPiece(currentPosition, resultantPiece);
                } else if (searchedPiece != null && !currentPosition.equals(startPosition)) {
                    potentialBoard.addPiece(currentPosition, searchedPiece);
                }
            }
        }

        // Return new potential board
        return potentialBoard;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        // If not in check but no valid moves available, stalemate
        return getValidMovesForBoard(currentBoard, teamColor).isEmpty();
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return currentTurn == chessGame.currentTurn && Objects.equals(currentBoard, chessGame.currentBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentTurn, currentBoard);
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "currentTurn=" + currentTurn +
                ", currentBoard=" + currentBoard +
                '}';
    }
}
