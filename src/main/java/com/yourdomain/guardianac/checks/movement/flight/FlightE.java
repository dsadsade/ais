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
 * Flight (E) — Packet flight detection.
 * Flags position teleport flight where the player clips through air via packets.
 */
public class FlightE extends Check {

    private static final double TELEPORT_THRESHOLD = 5.0;

    public FlightE(GuardianAC plugin) {
        super(plugin, "Flight", "E", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight() || player.isInsideVehicle()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        double dist = from.distance(to);

        if (!player.isOnGround() && dist > TELEPORT_THRESHOLD && !player.isRiptiding()) {
            handleViolation(data, player, 8.0, 10.0, 3.0);
        }
    }
}
