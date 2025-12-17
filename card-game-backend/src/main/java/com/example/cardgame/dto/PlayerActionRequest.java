package com.example.cardgame.dto;

public class PlayerActionRequest {
    private String type; // JOIN, BID, PLAY_CARD, etc
    private String playerName;
    private String gameId;
    private String cardSuit;
    private String cardRank;
    private Integer bidValue;
    private String bidSuit;
    private Boolean pass;
    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    public String getCardSuit() { return cardSuit; }
    public void setCardSuit(String cardSuit) { this.cardSuit = cardSuit; }
    public String getCardRank() { return cardRank; }
    public void setCardRank(String cardRank) { this.cardRank = cardRank; }
    public Integer getBidValue() { return bidValue; }
    public void setBidValue(Integer bidValue) { this.bidValue = bidValue; }
    public String getBidSuit() { return bidSuit; }
    public void setBidSuit(String bidSuit) { this.bidSuit = bidSuit; }
    public Boolean getPass() { return pass; }
    public void setPass(Boolean pass) { this.pass = pass; }
}
