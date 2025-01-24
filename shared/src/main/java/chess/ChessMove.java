package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private final ChessPosition from;
    private final ChessPosition to;
    private final ChessPiece.PieceType promotionType;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.from = startPosition;
        this.to = endPosition;
        this.promotionType = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return from;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return to;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        } else if (this == o) {
            return true;
        }

        ChessMove chessMove = (ChessMove) o;
        return Objects.equals(from, chessMove.from) &&
                Objects.equals(to, chessMove.to) &&
                promotionType == chessMove.promotionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, promotionType);
    }

    @Override
    public String toString() {
        return "CHESSMOVE(" + from + " to " + to + "; Promoted: " + promotionType + ")";
    }
}
