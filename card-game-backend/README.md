# Card Game Backend

This is a scalable, real-time backend for a multiplayer card game using Java 17, Spring Boot, and WebSocket.

## Running the Backend

1. **Install Java 17 and Maven.**
2. In the project root, run:
   ```bash
   mvn spring-boot:run
   ```
   The server will start on http://localhost:8080

## WebSocket API (Manual Testing)

Connect to: `ws://localhost:8080/ws/game`
- You can use [websocat](https://github.com/vi/websocat), [wscat](https://github.com/websockets/wscat), or a browser client.

### Example Messages

- **Join or Create a Room:**
  ```json
  { "type": "JOIN", "playerName": "Alice", "gameId": null }
  ```
  (If gameId is null/omitted, backend creates new room and returns gameId)

- **Bid:**
  ```json
  { "type": "BID", "playerName": "Alice", "gameId": "...", "bidValue": 80, "bidSuit": "HEARTS", "pass": false }
  ```

- **Play a Card:**
  ```json
  { "type": "PLAY_CARD", "playerName": "Alice", "gameId": "...", "cardSuit": "SPADES", "cardRank": "ACE" }
  ```

### Game State Updates
When the state updates, all players in the room get a broadcast:
```json
{
  "type": "GAME_STATE_UPDATE",
  "gameId": "...",
  "players": [ ... ],
  "phase": "PLAYING",
  ...
}
```

---

## Scaling/Architecture
- RoomManager manages all active rooms (up to 10,000+ players), each room is isolated.
- GameRoom owns all state for a single match and applies all relevant business/game logic.
- All client-server traffic uses strict DTOs for safety/modularity.

## Next Steps
- Implement frontend WebSocket client for real game UI.
- Add persistence and advanced monitoring as needed for production.
