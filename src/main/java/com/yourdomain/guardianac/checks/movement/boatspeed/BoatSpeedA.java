package com.yourdomain.guardianac.checks.movement.boatspeed;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * BoatSpeed (A) — Boat acceleration exceeding max.
 * Flags movement speed in boats beyond vanilla limits.
 */
public class BoatSpeedA extends Check {

    private static final double MAX_BOAT_SPEED = 0.6;

    public BoatSpeedA(GuardianAC plugin) {
        super(plugin, "BoatSpeed", "A", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (isExempt(player)) return;

        if (!player.isInsideVehicle() || !(player.getVehicle() instanceof Boat)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double horizDist = Math.hypot(event.getTo().getX() - event.getFrom().getX(),
                event.getTo().getZ() - event.getFrom().getZ());

        if (horizDist > MAX_BOAT_SPEED) {
            handleViolation(data, player, 3.0, 12.0, 1.5);
        }
    }
}
