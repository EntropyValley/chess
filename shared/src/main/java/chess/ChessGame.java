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
        TeamColor movingColor = existingPiece.getTeamColor();

        Collection<ChessMove> validPieceMoves = validMoves(startPosition);

        if (validPieceMoves.contains(move)) {
            currentBoard.addPiece(startPosition, null);
            currentBoard.addPiece(
                endPosition,
                promotionPieceType != null ? new ChessPiece(movingColor, promotionPieceType) : existingPiece
            );
        } else {
            throw new InvalidMoveException("Invalid Move:" + move);
        }
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

    private boolean arbitraryBoardCheck(TeamColor teamColor, ChessBoard boardToCheck) {
        final ChessPosition kingPosition = findKingPosition(teamColor, boardToCheck);

        // Check for found king
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
                                move -> move.getEndPosition() == kingPosition)
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

    private static ChessPosition findKingPosition(TeamColor teamColor, ChessBoard boardToCheck) {
        ChessPosition kingPosition = null;

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

        HashSet<ChessMove> validMoves = getValidMovesForBoard(currentBoard, teamColor);
        return validMoves.isEmpty();
    }

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

    private HashSet<ChessMove> getValidPositionMovesForBoard(ChessBoard boardToSearch, TeamColor teamColor, ChessPosition currentPosition) {
        ChessPiece currentPiece = boardToSearch.getPiece(currentPosition);
        HashSet<ChessMove> validPieceMoves = new HashSet<>();

        if (currentPiece != null) {
            if (currentPiece.getTeamColor() == teamColor) {
                Collection<ChessMove> pieceMoves = currentPiece.pieceMoves(boardToSearch, currentPosition);

                for (ChessMove move : pieceMoves) {
                    ChessBoard potentialBoard = createPotentialBoard(boardToSearch, move);
                    boolean potentialInCheck = arbitraryBoardCheck(teamColor, potentialBoard);

                    if (!potentialInCheck) {
                        validPieceMoves.add(move);
                    }
                }
            }
        }
        return validPieceMoves;
    }

    private static ChessBoard createPotentialBoard(ChessBoard boardToSearch, ChessMove move) {
        ChessBoard potentialBoard = new ChessBoard();
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        ChessPiece.PieceType promotionPiece = move.getPromotionPiece();
        ChessPiece currentPiece = boardToSearch.getPiece(startPosition);
        ChessPiece resultantPiece = promotionPiece == null ?
                currentPiece :
                new ChessPiece(currentPiece.getTeamColor(), promotionPiece);

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

        HashSet<ChessMove> validMoves = getValidMovesForBoard(currentBoard, teamColor);
        return validMoves.isEmpty();
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
