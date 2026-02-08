package com.yourdomain.guardianac.checks.combat.velocity;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * Velocity (E) — Direction manipulation detection.
 * Detects players who take knockback in the wrong direction by redirecting
 * KB vectors to move favorably instead of away from the attacker.
 */
public class VelocityE extends Check {

    public VelocityE(GuardianAC plugin) {
        super(plugin, "Velocity", "E", CheckType.COMBAT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Vector expected = player.getLocation().toVector()
                .subtract(attacker.getLocation().toVector()).normalize();

        getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
            if (!player.isOnline()) return;
            Vector actual = player.getVelocity();
            if (actual.lengthSquared() < 0.01) return;

            Vector actualH = new Vector(actual.getX(), 0, actual.getZ()).normalize();
            Vector expectedH = new Vector(expected.getX(), 0, expected.getZ()).normalize();

            double dot = actualH.dot(expectedH);
            if (dot < -0.3) {
                handleViolation(data, player, 5.0, 10.0, 1.0);
            }
        }, 2L);
    }
}
