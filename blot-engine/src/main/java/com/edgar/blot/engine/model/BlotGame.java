package com.edgar.blot.engine.model;

import java.util.*;

public final class BlotGame {

    private static final int TARGET_SCORE = 301;
    private static final int MIN_BID_POINTS = 8;

    private static final Map<Integer, Integer> CLOCKWISE_NEXT_PLAYER = Map.of(
            0, 3,
            3, 1,
            1, 2,
            2, 0
    );

    public enum GamePhase {
        BIDDING,
        PLAYING
    }

    public record Bid(int playerIndex, Suit suit, int points) {
    }

    public record PlayedCard(int playerIndex, Card card) {
    }

    private int team1TotalScore;
    private int team2TotalScore;

    private final List<Player> players;
    private final Team team1;
    private final Team team2;
    private final Deck deck = new Deck();

    private final Map<Integer, Set<Card>> playedCardsByPlayer = new HashMap<>();
    private final List<PlayedCard> tableCards = new ArrayList<>();

    private int currentPlayerIndex = 1;
    private int roundStartingPlayerIndex = 1;

    private GamePhase phase = GamePhase.BIDDING;
    private Bid highestBid = null;
    private int consecutivePasses = 0;

    private Suit trumpSuit = null;
    private boolean noTrump = true;

    private boolean trickCompleted = false;
    private int lastTrickWinnerIndex = -1;

    public BlotGame(List<Player> players) {
        Objects.requireNonNull(players, "players must not be null");

        if (players.size() != 4) {
            throw new IllegalArgumentException("Game requires exactly 4 players");
        }

        this.players = List.copyOf(players);
        this.team1 = new Team(this.players.get(0), this.players.get(2));
        this.team2 = new Team(this.players.get(1), this.players.get(3));

        for (int i = 0; i < 4; i++) {
            playedCardsByPlayer.put(i, new HashSet<>());
        }
    }

    public void prepareRound(int startingPlayerIndex) {
        validatePlayerIndex(startingPlayerIndex);

        deck.shuffle();

        roundStartingPlayerIndex = startingPlayerIndex;
        currentPlayerIndex = startingPlayerIndex;

        team1.clearWonCards();
        team2.clearWonCards();

        phase = GamePhase.BIDDING;
        highestBid = null;
        consecutivePasses = 0;

        trumpSuit = null;
        noTrump = true;

        tableCards.clear();
        trickCompleted = false;
        lastTrickWinnerIndex = -1;

        for (Set<Card> cards : playedCardsByPlayer.values()) {
            cards.clear();
        }
    }

    public void dealToPlayer(int playerIndex, int count) {
        validatePlayerIndex(playerIndex);

        Player player = players.get(playerIndex);

        for (int i = 0; i < count; i++) {
            player.receiveCard(deck.dealCard());
        }
    }

    public boolean makeBid(int playerIndex, Suit suit, int points) {
        if (phase != GamePhase.BIDDING) return false;
        if (playerIndex != currentPlayerIndex) return false;
        if (suit == null) return false;
        if (points < MIN_BID_POINTS) return false;
        if (highestBid != null && points <= highestBid.points()) return false;

        highestBid = new Bid(playerIndex, suit, points);
        consecutivePasses = 0;
        currentPlayerIndex = getNextClockwisePlayer(currentPlayerIndex);

        return true;
    }

    public boolean passBid(int playerIndex) {
        if (phase != GamePhase.BIDDING) return false;
        if (playerIndex != currentPlayerIndex) return false;

        consecutivePasses++;

        if (consecutivePasses >= 4) {
            startPlayingAfterBidding();
        } else {
            currentPlayerIndex = getNextClockwisePlayer(currentPlayerIndex);
        }

        return true;
    }

    private void startPlayingAfterBidding() {
        phase = GamePhase.PLAYING;

        if (highestBid != null) {
            trumpSuit = highestBid.suit();
            noTrump = false;
        } else {
            trumpSuit = null;
            noTrump = true;
        }

        currentPlayerIndex = roundStartingPlayerIndex;
        consecutivePasses = 0;
    }

    public boolean playCard(int playerIndex, Card card) {
        if (phase != GamePhase.PLAYING) return false;
        if (trickCompleted) return false;
        if (playerIndex != currentPlayerIndex) return false;
        if (card == null) return false;

        Player player = players.get(playerIndex);

        if (!player.getHand().contains(card)) return false;

        Set<Card> playedSet = playedCardsByPlayer.get(playerIndex);

        if (playedSet.contains(card)) return false;

        if (!isLegalCardPlay(playerIndex, card)) {
            return false;
        }

        playedSet.add(card);
        tableCards.add(new PlayedCard(playerIndex, card));

        if (tableCards.size() == 4) {
            lastTrickWinnerIndex = determineTrickWinner();

            List<Card> wonCards = tableCards.stream()
                    .map(PlayedCard::card)
                    .toList();

            if (isWePlayer(lastTrickWinnerIndex)) {
                team1.addWonCards(wonCards);
            } else {
                team2.addWonCards(wonCards);
            }

            currentPlayerIndex = lastTrickWinnerIndex;
            trickCompleted = true;
        }else {
            currentPlayerIndex = getNextClockwisePlayer(currentPlayerIndex);
        }

        return true;
    }

    private boolean isLegalCardPlay(int playerIndex, Card card) {
        if (tableCards.isEmpty()) {
            return true;
        }

        Suit leadSuit = tableCards.get(0).card().getSuit();

        if (card.getSuit() == leadSuit) {
            return true;
        }

        return !playerHasSuit(playerIndex, leadSuit);
    }

    private boolean playerHasSuit(int playerIndex, Suit suit) {
        for (Card card : getVisibleSortedHand(playerIndex)) {
            if (card.getSuit() == suit) {
                return true;
            }
        }

        return false;
    }

    private int determineTrickWinner() {
        PlayedCard first = tableCards.get(0);
        Suit leadSuit = first.card().getSuit();

        PlayedCard winner = first;

        for (int i = 1; i < tableCards.size(); i++) {
            PlayedCard candidate = tableCards.get(i);

            if (isCardStronger(candidate.card(), winner.card(), leadSuit)) {
                winner = candidate;
            }
        }

        return winner.playerIndex();
    }

    private boolean isCardStronger(Card candidate, Card currentWinner, Suit leadSuit) {
        Suit candidateSuit = candidate.getSuit();
        Suit winnerSuit = currentWinner.getSuit();

        if (!noTrump && trumpSuit != null) {
            boolean candidateTrump = candidateSuit == trumpSuit;
            boolean winnerTrump = winnerSuit == trumpSuit;

            if (candidateTrump && !winnerTrump) return true;
            if (!candidateTrump && winnerTrump) return false;

            if (candidateTrump) {
                return getTrumpPower(candidate) > getTrumpPower(currentWinner);
            }
        }

        if (candidateSuit == leadSuit && winnerSuit != leadSuit) return true;
        if (candidateSuit != leadSuit) return false;

        return getNormalPower(candidate) > getNormalPower(currentWinner);
    }

    private int getNormalPower(Card card) {
        return switch (card.getRank()) {
            case ACE -> 8;
            case TEN -> 7;
            case KING -> 6;
            case QUEEN -> 5;
            case JACK -> 4;
            case NINE -> 3;
            case EIGHT -> 2;
            case SEVEN -> 1;
        };
    }

    private int getTrumpPower(Card card) {
        return switch (card.getRank()) {
            case JACK -> 8;
            case NINE -> 7;
            case ACE -> 6;
            case TEN -> 5;
            case KING -> 4;
            case QUEEN -> 3;
            case EIGHT -> 2;
            case SEVEN -> 1;
        };
    }

    public void clearCompletedTrick() {
        if (!trickCompleted) return;

        tableCards.clear();
        trickCompleted = false;
    }

    public List<Card> getVisibleSortedHand(int playerIndex) {
        validatePlayerIndex(playerIndex);

        Player player = players.get(playerIndex);
        Set<Card> played = playedCardsByPlayer.get(playerIndex);

        List<Card> result = new ArrayList<>();

        for (Card card : player.getHand()) {
            if (!played.contains(card)) {
                result.add(card);
            }
        }

        result.sort((c1, c2) -> {
            int suitCompare = Integer.compare(c1.getSuit().ordinal(), c2.getSuit().ordinal());
            if (suitCompare != 0) return suitCompare;
            return Integer.compare(c2.getRank().ordinal(), c1.getRank().ordinal());
        });

        return result;
    }

    private int getNextClockwisePlayer(int playerIndex) {
        Integer next = CLOCKWISE_NEXT_PLAYER.get(playerIndex);

        if (next == null) {
            throw new IllegalArgumentException("Unknown player index: " + playerIndex);
        }

        return next;
    }

    private void validatePlayerIndex(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= 4) {
            throw new IllegalArgumentException("Player index must be between 0 and 3: " + playerIndex);
        }
    }

    public int getWeScore() {
        return team1.calculatePoints(trumpSuit, noTrump);
    }

    public int getTheyScore() {
        return team2.calculatePoints(trumpSuit, noTrump);
    }

    public GamePhase getPhase() {
        return phase;
    }

    public Bid getHighestBid() {
        return highestBid;
    }

    public int getConsecutivePasses() {
        return consecutivePasses;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public boolean isTrickCompleted() {
        return trickCompleted;
    }

    public int getLastTrickWinnerIndex() {
        return lastTrickWinnerIndex;
    }

    public List<PlayedCard> getTableCards() {
        return List.copyOf(tableCards);
    }

    public int getTeam1TotalScore() {
        return team1TotalScore;
    }

    public int getTeam2TotalScore() {
        return team2TotalScore;
    }

    public List<Player> getPlayers() {
        return players;
    }

    private boolean isWePlayer(int playerIndex) {
        return playerIndex == 0 || playerIndex == 2;
    }

    public boolean isFinished() {
        return team1TotalScore >= TARGET_SCORE || team2TotalScore >= TARGET_SCORE;
    }

    public Team getWinner() {
        if (!isFinished()) return null;
        return team1TotalScore >= team2TotalScore ? team1 : team2;
    }
}