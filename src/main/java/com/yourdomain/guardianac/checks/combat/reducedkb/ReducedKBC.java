package com.yourdomain.guardianac.checks.combat.reducedkb;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * ReducedKB (C) — Directional knockback modification.
 * Detects KB that is applied in the wrong direction—player moves toward
 * attacker instead of away, indicating velocity packet manipulation.
 */
public class ReducedKBC extends Check {

    public ReducedKBC(GuardianAC plugin) {
        super(plugin, "ReducedKB", "C", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getDamager() instanceof org.bukkit.entity.LivingEntity attacker)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (!data.isPendingVelocityCheck()) return;

        Vector kbDirection = player.getLocation().toVector()
                .subtract(attacker.getLocation().toVector()).normalize();

        Vector actualVelocity = player.getVelocity();
        Vector horizontalVel = new Vector(actualVelocity.getX(), 0, actualVelocity.getZ());

        if (horizontalVel.length() < 0.05) return;

        double dot = horizontalVel.normalize().dot(kbDirection);

        if (dot < -0.3) {
            handleViolation(data, player, 6.0, 10.0, 0.5);
        }
    }
}
