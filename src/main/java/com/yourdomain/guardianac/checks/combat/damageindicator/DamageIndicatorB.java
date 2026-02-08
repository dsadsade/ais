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
 * DamageIndicator (B) — Target switch to low HP entity.
 * Detects when a player switches targets mid-fight specifically to
 * a lower HP entity they shouldn't know the health of.
 */
public class DamageIndicatorB extends Check {

    private static final double HP_SWITCH_THRESHOLD = 0.4;

    public DamageIndicatorB(GuardianAC plugin) {
        super(plugin, "DamageIndicator", "B", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        int lastTargetId = data.getLastTargetEntityId();
        if (lastTargetId == -1 || lastTargetId == target.getEntityId()) return;

        double targetHpRatio = target.getHealth() / target.getMaxHealth();

        if (targetHpRatio < HP_SWITCH_THRESHOLD) {
            long timeSinceLastAttack = System.currentTimeMillis() - data.getLastAttackTime();
            if (timeSinceLastAttack < 1000) {
                int flags = data.getDamageIndicatorFlags() + 1;
                data.setDamageIndicatorFlags(flags);

                if (flags >= 4) {
                    handleViolation(data, player, 4.0, 12.0, 1.0);
                    data.setDamageIndicatorFlags(0);
                }
            }
        }
    }
}
