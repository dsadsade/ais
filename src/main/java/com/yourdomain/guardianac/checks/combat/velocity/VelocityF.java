package com.yourdomain.guardianac.checks.combat.velocity;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Velocity (F) — Vertical velocity manipulation.
 * Specifically checks the Y component of knockback. Many velocity mods
 * only reduce vertical KB to prevent being launched upward.
 */
public class VelocityF extends Check {

    private static final double MIN_EXPECTED_Y = 0.35;

    public VelocityF(GuardianAC plugin) {
        super(plugin, "Velocity", "F", CheckType.COMBAT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
            if (!player.isOnline()) return;
            data.setLastVelocity(player.getVelocity().getX(),
                    player.getVelocity().getY(), player.getVelocity().getZ());
            data.setLastDamageTime(System.currentTimeMillis());
            data.setPendingVelocityCheck(true);
        }, 1L);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null || !data.isPendingVelocityCheck()) return;
        if (player.isOnGround()) return;

        long elapsed = System.currentTimeMillis() - data.getLastDamageTime();
        if (elapsed > 100 && elapsed < 400) {
            double expectedY = data.getLastVelocityY();
            double actualDeltaY = event.getTo().getY() - event.getFrom().getY();

            if (expectedY > MIN_EXPECTED_Y && actualDeltaY < expectedY * 0.5) {
                handleViolation(data, player, 4.0, 10.0, 1.0);
            }
            data.setPendingVelocityCheck(false);
        }
    }
}
