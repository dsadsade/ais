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
 * DamageIndicator (A) — Health ESP targeting detection.
 * Detects players who preferentially target low-HP entities they
 * shouldn't be able to see the health of, indicating ESP/health display.
 */
public class DamageIndicatorA extends Check {

    private static final double LOW_HP_THRESHOLD = 0.3;
    private static final int FLAG_THRESHOLD = 5;

    public DamageIndicatorA(GuardianAC plugin) {
        super(plugin, "DamageIndicator", "A", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double healthRatio = target.getHealth() / target.getMaxHealth();

        if (healthRatio < LOW_HP_THRESHOLD) {
            int lastTargetId = data.getLastTargetEntityId();
            if (lastTargetId != -1 && lastTargetId != target.getEntityId()) {
                int flags = data.getDamageIndicatorFlags() + 1;
                data.setDamageIndicatorFlags(flags);

                if (flags >= FLAG_THRESHOLD) {
                    handleViolation(data, player, 4.0, 12.0, 1.0);
                    data.setDamageIndicatorFlags(0);
                }
            }
        } else {
            data.setDamageIndicatorFlags(Math.max(0, data.getDamageIndicatorFlags() - 1));
        }
    }
}
