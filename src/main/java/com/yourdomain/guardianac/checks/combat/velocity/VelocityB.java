package com.yourdomain.guardianac.checks.combat.velocity;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.PingUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * Velocity (B) — Vertical knockback check.
 * Ensures players gain upward velocity after taking hit (Y component of knockback).
 * AntiKB clients often zero out vertical knockback.
 */
public class VelocityB extends Check {

    private static final double MIN_VERTICAL_RATIO = 0.20;

    public VelocityB(GuardianAC plugin) {
        super(plugin, "Velocity", "B", CheckType.COMBAT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        int delay = PingUtil.getCompensationTicks(player) + 2;

        // Capture expected velocity 1 tick after damage (when Bukkit sets it)
        getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
            if (!player.isOnline()) return;
            double expectedY = player.getVelocity().getY();
            data.setLastVelocity(0, expectedY, 0);

            if (expectedY < 0.1) return; // Not enough vertical KB to check

            // Check actual Y velocity after compensation delay
            getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
                if (!player.isOnline()) return;
                if (isAgainstWall(player)) return;

                // After delay, player should have moved upward
                // If on ground already, their Y velocity has been consumed — check movement history
                Vector currentVel = player.getVelocity();

                // If the player is on ground already (knockback was absorbed by landing), skip
                if (player.isOnGround() && currentVel.getY() < 0) return;

                double ratio = Math.abs(currentVel.getY()) / Math.abs(expectedY);
                if (ratio < MIN_VERTICAL_RATIO && !player.isOnGround()) {
                    handleViolation(data, player, 2.0, 6.0, 0.5);
                }
            }, delay);
        }, 1L);
    }

    private boolean isAgainstWall(Player player) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                if (player.getLocation().clone().add(x * 0.4, 0, z * 0.4).getBlock().getType().isSolid()) {
                    return true;
                }
            }
        }
        // Also check above (ceiling absorbs vertical KB)
        if (player.getLocation().clone().add(0, 2.0, 0).getBlock().getType().isSolid()) {
            return true;
        }
        return false;
    }
}
