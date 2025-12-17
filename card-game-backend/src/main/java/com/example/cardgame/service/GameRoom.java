package com.example.cardgame.service;

import com.example.cardgame.model.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameRoom {
    private final UUID gameId;
    private final List<Player> players = new CopyOnWriteArrayList<>();
    private final Map<TeamType, Team> teams = new EnumMap<>(TeamType.class);
    private final List<List<Card>> tricks = new ArrayList<>();
    private final List<Bid> bids = new ArrayList<>();
    private final Map<TeamType, Integer> scores = new EnumMap<>(TeamType.class);
    private volatile Phase phase = Phase.WAITING;
    private volatile Suit trumpSuit;
    private volatile Player dealer;
    private volatile Player currentPlayer;
    private List<Card> deck = new ArrayList<>();

    public GameRoom(UUID gameId) {
        this.gameId = gameId;
        for (TeamType type : TeamType.values()) {
            teams.put(type, new Team(type, new ArrayList<>()));
            scores.put(type, 0);
        }
    }
    // Add methods: addPlayer, removePlayer, applyMove(PlayerAction), getState, ...
    public UUID getGameId() { return gameId; }
    public List<Player> getPlayers() { return players; }
    public Map<TeamType, Team> getTeams() { return teams; }
    public Phase getPhase() { return phase; }
    public void setPhase(Phase phase) { this.phase = phase; }
    public Suit getTrumpSuit() { return trumpSuit; }
    public void setTrumpSuit(Suit trumpSuit) { this.trumpSuit = trumpSuit; }
    public Player getDealer() { return dealer; }
    public void setDealer(Player dealer) { this.dealer = dealer; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public void setCurrentPlayer(Player cp) { this.currentPlayer = cp; }
    public List<List<Card>> getTricks() { return tricks; }
    public List<Bid> getBids() { return bids; }
    public Map<TeamType, Integer> getScores() { return scores; }
    public List<Card> getDeck() { return deck; }
    public void setDeck(List<Card> deck) { this.deck = deck; }
}
