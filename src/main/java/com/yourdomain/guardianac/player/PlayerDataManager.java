package com.yourdomain.guardianac.player;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final Map<UUID, PlayerData> playerDataMap;

    public PlayerDataManager() {
        this.playerDataMap = new ConcurrentHashMap<>();
    }

    public void addPlayer(Player player) {
        playerDataMap.put(player.getUniqueId(), new PlayerData(player));
    }

    public void removePlayer(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.get(player.getUniqueId());
    }

    /** Returns all active player data entries (thread-safe snapshot). */
    public Collection<PlayerData> getAllPlayerData() {
        return playerDataMap.values();
    }
}
