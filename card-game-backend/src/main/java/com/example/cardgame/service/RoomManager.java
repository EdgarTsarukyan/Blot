package com.example.cardgame.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;

public class RoomManager {
    private static final RoomManager INSTANCE = new RoomManager();
    private final ConcurrentHashMap<UUID, GameRoom> rooms = new ConcurrentHashMap<>();

    public static RoomManager getInstance() {
        return INSTANCE;
    }

    public UUID createRoom(GameRoom gameRoom) {
        UUID id = gameRoom.getGameId();
        rooms.put(id, gameRoom);
        return id;
    }

    public Optional<GameRoom> getRoom(UUID id) {
        return Optional.ofNullable(rooms.get(id));
    }

    public void removeRoom(UUID id) {
        rooms.remove(id);
    }

    public int roomCount() {
        return rooms.size();
    }
}
