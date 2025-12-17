package com.example.cardgame.service;

import com.example.cardgame.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class GameService {
    // Shuffles and deals cards to each player in the game
    public void dealCards(Game game) {
        List<Card> deck = generateDeck();
        Collections.shuffle(deck);
        List<List<Card>> hands = new ArrayList<>();
        for (int i = 0; i < 4; i++) hands.add(new ArrayList<>());
        for (int i = 0; i < deck.size(); i++) {
            hands.get(i % 4).add(deck.get(i));
        }
        for (int i = 0; i < 4; i++) {
            game.getPlayers().get(i).setHand(hands.get(i));
        }
        game.getDeck().clear();
        game.getDeck().addAll(deck);
        game.setPhase(Phase.BIDDING);
    }

    // Generates a standard 32-card deck (7, 8, 9, 10, J, Q, K, A in 4 suits)
    public List<Card> generateDeck() {
        List<Card> deck = new ArrayList<>();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
        return deck;
    }

    // Returns the next player in clockwise order
    public Player getNextPlayer(Game game, Player current) {
        List<Player> orderedPlayers = game.getPlayers();
        int idx = orderedPlayers.indexOf(current);
        return orderedPlayers.get((idx + 1) % orderedPlayers.size());
    }

    // Example: Advances the phase to next phase
    public void advancePhase(Game game) {
        switch (game.getPhase()) {
            case WAITING: game.setPhase(Phase.DEALING); break;
            case DEALING: game.setPhase(Phase.BIDDING); break;
            case BIDDING: game.setPhase(Phase.PLAYING); break;
            case PLAYING: game.setPhase(Phase.SCORING); break;
            case SCORING: game.setPhase(Phase.WAITING); break;
        }
    }

    // Add more methods: bid, playCard, validatePlay, etc. later
}
