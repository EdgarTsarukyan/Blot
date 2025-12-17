package com.example.cardgame.dto;

import com.example.cardgame.model.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GameStateResponse {
    private String type = "GAME_STATE_UPDATE";
    private UUID gameId;
    private List<Player> players;
    private Phase phase;
    private Suit trumpSuit;
    private Player dealer;
    private Player currentPlayer;
    private List<List<Card>> tricks;
    private Map<TeamType, Integer> scores;
    // Getters and setters
    public String getType() { return type; }
    public UUID getGameId() { return gameId; }
    public void setGameId(UUID gameId) { this.gameId = gameId; }
    public List<Player> getPlayers() { return players; }
    public void setPlayers(List<Player> players) { this.players = players; }
    public Phase getPhase() { return phase; }
    public void setPhase(Phase phase) { this.phase = phase; }
    public Suit getTrumpSuit() { return trumpSuit; }
    public void setTrumpSuit(Suit trumpSuit) { this.trumpSuit = trumpSuit; }
    public Player getDealer() { return dealer; }
    public void setDealer(Player dealer) { this.dealer = dealer; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(Player currentPlayer) { this.currentPlayer = currentPlayer; }
    public List<List<Card>> getTricks() { return tricks; }
    public void setTricks(List<List<Card>> tricks) { this.tricks = tricks; }
    public Map<TeamType, Integer> getScores() { return scores; }
    public void setScores(Map<TeamType, Integer> scores) { this.scores = scores; }
}
