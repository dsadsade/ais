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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

/**
 * Velocity (C) — Percentage-based knockback analysis.
 * Measures the ratio of actual velocity taken to expected velocity.
 * Unlike A/B which check H/V separately, this checks the overall magnitude
 * ratio. Effective against subtle velocity modifiers (e.g. 85% knockback).
 */
public class VelocityC extends Check {

    private static final double MIN_VELOCITY_RATIO = 0.65;
    private static final double MIN_EXPECTED_MAGNITUDE = 0.15;

    public VelocityC(GuardianAC plugin) {
        super(plugin, "Velocity", "C", CheckType.COMBAT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
            if (!player.isOnline()) return;
            Vector vel = player.getVelocity();
            data.setLastVelocity(vel.getX(), vel.getY(), vel.getZ());
            data.setLastDamageTime(System.currentTimeMillis());
            data.setPendingVelocityCheck(true);
        }, 1L);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null || !data.isPendingVelocityCheck()) return;

        int compensationTicks = PingUtil.getCompensationTicks(player);
        long expectedDelay = compensationTicks * 50L + 150L;

        long elapsed = System.currentTimeMillis() - data.getLastDamageTime();
        if (elapsed < expectedDelay) return;

        data.setPendingVelocityCheck(false);

        double expectedMag = Math.sqrt(
                data.getLastVelocityX() * data.getLastVelocityX()
                + data.getLastVelocityY() * data.getLastVelocityY()
                + data.getLastVelocityZ() * data.getLastVelocityZ());

        if (expectedMag < MIN_EXPECTED_MAGNITUDE) return;

        Vector actual = player.getVelocity();
        double actualMag = actual.length();

        // Players against walls legitimately lose velocity
        if (isAgainstWall(player)) return;

        double ratio = actualMag / expectedMag;

        if (ratio < MIN_VELOCITY_RATIO) {
            double deficit = MIN_VELOCITY_RATIO - ratio;
            double weight = Math.min(3.5, 1.0 + deficit * 8.0);
            handleViolation(data, player, weight, 8.0, 0.5);
        }
    }

    private boolean isAgainstWall(Player player) {
        org.bukkit.Location loc = player.getLocation();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                if (loc.clone().add(x * 0.4, 0, z * 0.4).getBlock().getType().isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }
}