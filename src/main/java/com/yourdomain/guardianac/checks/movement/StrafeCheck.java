package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Detects Strafe hacks — impossible air acceleration and direction changes.
 * In vanilla, air strafing has very limited acceleration (~2% per tick).
 * Strafe hacks allow full ground-like acceleration while airborne,
 * enabling sharp direction changes and speed maintenance mid-air.
 */
public class StrafeCheck extends Check {

    private static final double MAX_AIR_ACCEL = 0.04; // max horizontal acceleration per tick in air
    private static final int MIN_AIR_TICKS = 5; // only check after some air time

    public StrafeCheck(GuardianAC plugin) {
        super(plugin, "Strafe", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isInsideVehicle() || player.isGliding() || player.isSwimming()) return;
        if (player.isRiptiding()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (player.isOnGround()) {
            data.setImpossibleStrafeCount(0);
            data.setLastDeltaX(0);
            data.setLastDeltaZ(0);
            return;
        }

        if (data.getAirTicks() < MIN_AIR_TICKS) return;

        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();

        double prevDeltaX = data.getLastDeltaX();
        double prevDeltaZ = data.getLastDeltaZ();

        // Calculate acceleration (change in velocity)
        double accelX = Math.abs(deltaX - prevDeltaX);
        double accelZ = Math.abs(deltaZ - prevDeltaZ);
        double totalAccel = Math.sqrt(accelX * accelX + accelZ * accelZ);

        if (totalAccel > MAX_AIR_ACCEL) {
            data.setImpossibleStrafeCount(data.getImpossibleStrafeCount() + 1);

            if (data.getImpossibleStrafeCount() >= 4) {
                handleViolation(data, player, 4.0, 10.0, 1.0);
                data.setImpossibleStrafeCount(0);
            }
        } else {
            data.setImpossibleStrafeCount(Math.max(0, data.getImpossibleStrafeCount() - 1));
        }

        data.setLastDeltaX(deltaX);
        data.setLastDeltaZ(deltaZ);
    }
}
