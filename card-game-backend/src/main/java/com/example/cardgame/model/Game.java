package com.example.cardgame.model;

import java.util.*;

public class Game {
    private UUID id;
    private List<Player> players;
    private Map<TeamType, Team> teams;
    private List<Card> deck;
    private List<List<Card>> tricks; // each trick is a list of cards (in play order)
    private Phase phase;
    private Suit trumpSuit;
    private Player dealer;
    private Player currentPlayer;
    private List<Bid> bids;
    private Map<TeamType, Integer> scores;

    public Game(UUID id, List<Player> players, Map<TeamType, Team> teams) {
        this.id = id;
        this.players = players;
        this.teams = teams;
        this.deck = new ArrayList<>();
        this.tricks = new ArrayList<>();
        this.bids = new ArrayList<>();
        this.scores = new HashMap<>();
        this.phase = Phase.WAITING;
    }

    // Getters and setters omitted for brevity; implement as needed
    public UUID getId() { return id; }
    public List<Player> getPlayers() { return players; }
    public Map<TeamType, Team> getTeams() { return teams; }
    public List<Card> getDeck() { return deck; }
    public List<List<Card>> getTricks() { return tricks; }
    public Phase getPhase() { return phase; }
    public Suit getTrumpSuit() { return trumpSuit; }
    public Player getDealer() { return dealer; }
    public Player getCurrentPlayer() { return currentPlayer; }
    public List<Bid> getBids() { return bids; }
    public Map<TeamType, Integer> getScores() { return scores; }

    public void setPhase(Phase phase) { this.phase = phase; }
    public void setTrumpSuit(Suit trumpSuit) { this.trumpSuit = trumpSuit; }
    public void setDealer(Player dealer) { this.dealer = dealer; }
    public void setCurrentPlayer(Player currentPlayer) { this.currentPlayer = currentPlayer; }
}
