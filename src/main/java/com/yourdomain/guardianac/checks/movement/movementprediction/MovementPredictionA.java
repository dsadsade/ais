package com.yourdomain.guardianac.checks.movement.movementprediction;

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
 * MovementPrediction (A) — Client-server position prediction desync.
 * Compares expected position (based on last velocity) to actual position.
 */
public class MovementPredictionA extends Check {

    private static final double DESYNC_THRESHOLD = 0.5;

    public MovementPredictionA(GuardianAC plugin) {
        super(plugin, "MovementPrediction", "A", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isInsideVehicle() || player.isGliding()) return;
        if (player.isRiptiding()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        double actualDX = to.getX() - from.getX();
        double actualDZ = to.getZ() - from.getZ();
        double predictedDX = data.getLastDeltaX();
        double predictedDZ = data.getLastDeltaZ();

        double desync = Math.hypot(actualDX - predictedDX, actualDZ - predictedDZ);

        if (desync > DESYNC_THRESHOLD && data.getAirTicks() > 2) {
            handleViolation(data, player, 3.0, 12.0, 1.5);
        }

        data.setLastDeltaX(actualDX);
        data.setLastDeltaZ(actualDZ);
    }
}
