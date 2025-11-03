package com.yourdomain.guardianac.player;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {

    private final UUID playerUUID;
    private final Map<String, Integer> violationLevels;

    public PlayerData(Player player) {
        this.playerUUID = player.getUniqueId();
        this.violationLevels = new HashMap<>();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getViolationLevel(String checkName) {
        return violationLevels.getOrDefault(checkName, 0);
    }

    public void incrementViolationLevel(String checkName) {
        violationLevels.put(checkName, getViolationLevel(checkName) + 1);
    }

    public void resetViolationLevel(String checkName) {
        violationLevels.remove(checkName);
    }
}
