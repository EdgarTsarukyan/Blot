package com.example.cardgame.model;

public class Bid {
    private Player player;
    private int value; // contract (multiple of 10)
    private Suit suit; // null if no trump
    private boolean pass;

    public Bid(Player player, int value, Suit suit, boolean pass) {
        this.player = player;
        this.value = value;
        this.suit = suit;
        this.pass = pass;
    }

    public Player getPlayer() { return player; }
    public int getValue() { return value; }
    public Suit getSuit() { return suit; }
    public boolean isPass() { return pass; }
}
