package websocket.messages;

import model.GameData;

public class LoadGameMessage extends ServerMessage {
    GameData gameData;

    public LoadGameMessage(ServerMessageType type, GameData gameData) {
        super(type);
        this.gameData = gameData;
    }

    public GameData game() {
        return gameData;
    }
}
