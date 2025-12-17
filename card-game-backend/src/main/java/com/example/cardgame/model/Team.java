package com.example.cardgame.model;

import java.util.List;

public class Team {
    private TeamType type;
    private List<Player> members;
    private int score;

    public Team(TeamType type, List<Player> members) {
        this.type = type;
        this.members = members;
        this.score = 0;
    }

    public TeamType getType() { return type; }
    public List<Player> getMembers() { return members; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}
