package com.yourdomain.guardianac.checks.player.badpackets;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * BadPackets (B) — Impossible Position Detection
 * Detects players sending movement packets with impossible positions:
 * - NaN or Infinity coordinates
 * - Y values exceeding world height limits drastically
 * - Teleporting extremely far in a single tick
 */
public class BadPacketsB extends Check {

    private static final double MAX_SINGLE_TICK_DISTANCE = 100.0; // blocks
    private static final double MAX_Y = 1000.0;
    private static final double MIN_Y = -200.0;

    public BadPacketsB(GuardianAC plugin) {
        super(plugin, "BadPackets", "B", CheckType.PLAYER);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location to = event.getTo();

        // Check for NaN/Infinity
        if (Double.isNaN(to.getX()) || Double.isNaN(to.getY()) || Double.isNaN(to.getZ())
                || Double.isInfinite(to.getX()) || Double.isInfinite(to.getY()) || Double.isInfinite(to.getZ())) {
            handleViolation(data, player, 10.0, 5.0, 0.0);
            event.setCancelled(true);
            return;
        }

        // Check for extreme Y
        if (to.getY() > MAX_Y || to.getY() < MIN_Y) {
            handleViolation(data, player, 8.0, 5.0, 0.0);
            event.setCancelled(true);
            return;
        }

        // Check for teleportation
        double distance = event.getFrom().distance(to);
        if (distance > MAX_SINGLE_TICK_DISTANCE) {
            handleViolation(data, player, 10.0, 5.0, 0.0);
            event.setCancelled(true);
        }
    }
}
