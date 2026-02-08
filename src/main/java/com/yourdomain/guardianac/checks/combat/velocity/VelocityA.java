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
 * Velocity (A) — Horizontal knockback check.
 * Verifies that players actually move horizontally after taking knockback.
 * Stores expected velocity on damage, then checks actual movement ticks later.
 */
public class VelocityA extends Check {

    private static final double MIN_HORIZONTAL_RATIO = 0.25; // must move at least 25% of expected

    public VelocityA(GuardianAC plugin) {
        super(plugin, "Velocity", "A", CheckType.COMBAT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Schedule to capture velocity on the next tick (after Bukkit applies knockback)
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
        long expectedDelay = compensationTicks * 50L + 100L;

        long elapsed = System.currentTimeMillis() - data.getLastDamageTime();
        if (elapsed < expectedDelay) return; // Wait for compensation

        data.setPendingVelocityCheck(false);

        // Check horizontal movement
        double expectedHorizontal = Math.sqrt(
                data.getLastVelocityX() * data.getLastVelocityX()
                + data.getLastVelocityZ() * data.getLastVelocityZ());

        if (expectedHorizontal < 0.1) return; // Knockback too small to analyze

        Vector actualVelocity = player.getVelocity();
        double actualHorizontal = Math.sqrt(
                actualVelocity.getX() * actualVelocity.getX()
                + actualVelocity.getZ() * actualVelocity.getZ());

        // Check if player is against a wall (absorbs knockback legitimately)
        if (isAgainstWall(player)) return;

        double ratio = actualHorizontal / expectedHorizontal;
        if (ratio < MIN_HORIZONTAL_RATIO) {
            handleViolation(data, player, 2.5, 6.0, 0.5);
        }
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
        return false;
    }
}
