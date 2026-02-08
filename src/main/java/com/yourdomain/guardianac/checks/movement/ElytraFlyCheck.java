package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

/**
 * Detects Elytra exploits:
 * - Elytra speed hack (flying faster than allowed)
 * - Elytra climb (gaining altitude without firework rockets)
 * Vanilla max elytra speed is approximately 67.1 m/s (~3.35 blocks/tick).
 */
public class ElytraFlyCheck extends Check {

    private static final double MAX_ELYTRA_SPEED = 3.5; // blocks/tick
    private static final double MAX_ASCEND_PER_TICK = 0.5; // max upward velocity without fireworks
    private static final int SPEED_FLAG_THRESHOLD = 3;

    public ElytraFlyCheck(GuardianAC plugin) {
        super(plugin, "ElytraFly", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (!player.isGliding()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        Vector delta = to.toVector().subtract(from.toVector());

        double speed = delta.length();
        double deltaY = delta.getY();

        data.setLastElytraSpeed(speed);

        // Check 1: Excessive elytra speed
        if (speed > MAX_ELYTRA_SPEED) {
            data.setElytraSpeedFlags(data.getElytraSpeedFlags() + 1);
            if (data.getElytraSpeedFlags() >= SPEED_FLAG_THRESHOLD) {
                handleViolation(data, player, 5.0, 10.0, 1.0);
                data.setElytraSpeedFlags(0);
            }
        } else {
            data.setElytraSpeedFlags(Math.max(0, data.getElytraSpeedFlags() - 1));
        }

        // Check 2: Ascending without fireworks
        if (deltaY > MAX_ASCEND_PER_TICK && speed > 0.5) {
            // Check if player recently used a firework
            // Fireworks give a velocity boost; sustained climb without any is suspicious
            if (data.getAirTicks() > 20) {
                handleViolation(data, player, 3.0, 8.0, 0.5);
            }
        }
    }
}
