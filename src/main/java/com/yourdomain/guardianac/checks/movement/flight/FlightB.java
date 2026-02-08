package com.yourdomain.guardianac.checks.movement.flight;

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
 * Flight (B) — Creative-like flight in survival.
 * Detects smooth horizontal + vertical movement while airborne.
 */
public class FlightB extends Check {

    public FlightB(GuardianAC plugin) {
        super(plugin, "Flight", "B", CheckType.MOVEMENT);
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

        Location from = event.getFrom();
        Location to = event.getTo();
        double deltaY = to.getY() - from.getY();
        double horizDist = Math.hypot(to.getX() - from.getX(), to.getZ() - from.getZ());

        if (!player.isOnGround() && deltaY > 0.0 && deltaY < 0.15 && horizDist > 0.2) {
            data.setAirTicks(data.getAirTicks() + 1);
            if (data.getAirTicks() > 20) {
                handleViolation(data, player, 5.0, 12.0, 2.0);
            }
        } else if (player.isOnGround()) {
            data.setAirTicks(0);
        }
    }
}
