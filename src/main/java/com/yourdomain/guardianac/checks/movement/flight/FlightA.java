package com.yourdomain.guardianac.checks.movement.flight;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Flight (A) — Vertical hover detection.
 * Flags players staying at the same Y level while airborne.
 */
public class FlightA extends Check {

    public FlightA(GuardianAC plugin) {
        super(plugin, "Flight", "A", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight() || player.isGliding() || player.isInsideVehicle()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaY = Math.abs(event.getTo().getY() - event.getFrom().getY());

        if (!player.isOnGround() && deltaY < 0.01) {
            data.setHoverTicks(data.getHoverTicks() + 1);
            if (data.getHoverTicks() > 12) {
                handleViolation(data, player, 3.0, 10.0, 1.5);
            }
        } else {
            data.setHoverTicks(0);
        }
    }
}
