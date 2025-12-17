package com.example.cardgame.websocket;

import com.example.cardgame.model.*;
import com.example.cardgame.service.GameService;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

public class GameWebSocketHandler extends TextWebSocketHandler {
    private final Map<UUID, Game> games = new HashMap<>();
    private final Map<WebSocketSession, UUID> sessionToGame = new HashMap<>();
    private final GameService gameService = new GameService();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Stub: parse message and route
        // Example for now: expect simple {"type":"JOIN", "playerName":"Alice"}
        // To be expanded with real DTOs
    }

    private void broadcastGameState(Game game) {
        // Send updated game state to all players (pseudo, to be implemented)
        // For each player, get their session and send serialized state
    }
}
