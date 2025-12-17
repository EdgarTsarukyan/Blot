package com.example.cardgame.model;

import java.util.List;
import java.util.UUID;

public class Player {
    private UUID id;
    private String name;
    private List<Card> hand;
    private PlayerPosition position;
    private TeamType team;

    // Getters and setters omitted for brevity; can use Lombok in future.

    public Player(UUID id, String name, List<Card> hand, PlayerPosition position, TeamType team) {
        this.id = id;
        this.name = name;
        this.hand = hand;
        this.position = position;
        this.team = team;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public List<Card> getHand() { return hand; }
    public PlayerPosition getPosition() { return position; }
    public TeamType getTeam() { return team; }

    public void setHand(List<Card> hand) { this.hand = hand; }
    public void setPosition(PlayerPosition position) { this.position = position; }
    public void setTeam(TeamType team) { this.team = team; }
}
