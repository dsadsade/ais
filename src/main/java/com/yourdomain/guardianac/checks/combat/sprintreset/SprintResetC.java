package com.yourdomain.guardianac.checks.combat.sprintreset;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * SprintReset (C) — Sprint state desync detection.
 * Detects when the sprint state on server disagrees with velocity,
 * indicating client-side sprint packet manipulation.
 */
public class SprintResetC extends Check {

    public SprintResetC(GuardianAC plugin) {
        super(plugin, "SprintReset", "C", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        boolean serverSprinting = player.isSprinting();

        double velX = player.getVelocity().getX();
        double velZ = player.getVelocity().getZ();
        double horizontalSpeed = Math.sqrt(velX * velX + velZ * velZ);

        boolean velocityIndicatesSprinting = horizontalSpeed > 0.11;

        if (serverSprinting && !velocityIndicatesSprinting && !player.isBlocking()) {
            long timeSinceLastHit = System.currentTimeMillis() - data.getLastAttackTime();
            if (timeSinceLastHit < 200) {
                handleViolation(data, player, 3.0, 10.0, 1.0);
            }
        }
    }
}
