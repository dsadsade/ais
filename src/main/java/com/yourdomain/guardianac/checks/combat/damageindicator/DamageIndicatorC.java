package com.yourdomain.guardianac.checks.combat.damageindicator;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * DamageIndicator (C) — Kill confirmation timing.
 * Detects when a player immediately disengages after dealing a fatal
 * blow, before the death animation plays—indicating knowledge of
 * exact target HP remaining.
 */
public class DamageIndicatorC extends Check {

    public DamageIndicatorC(GuardianAC plugin) {
        super(plugin, "DamageIndicator", "C", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double remainingHp = target.getHealth() - event.getFinalDamage();

        if (remainingHp <= 0) {
            long timeSinceLastAttack = System.currentTimeMillis() - data.getLastAttackTime();

            if (timeSinceLastAttack > 0 && timeSinceLastAttack < 50) {
                float yawDelta = Math.abs(player.getLocation().getYaw() - data.getLastYaw());
                if (yawDelta > 180) yawDelta = 360 - yawDelta;

                if (yawDelta > 45) {
                    int flags = data.getDamageIndicatorFlags() + 1;
                    data.setDamageIndicatorFlags(flags);

                    if (flags >= 3) {
                        handleViolation(data, player, 4.0, 12.0, 1.0);
                        data.setDamageIndicatorFlags(0);
                    }
                }
            }
        }
    }
}
