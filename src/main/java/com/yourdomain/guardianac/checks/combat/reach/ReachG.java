package com.yourdomain.guardianac.checks.combat.reach;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Reach (G) — Attack reach with projectile entities.
 * Detects suspicious melee distances when combined with recent projectile use,
 * where the attacker is too far from the impact to have melee'd legitimately.
 */
public class ReachG extends Check {

    private static final double MAX_PROJECTILE_MELEE_RANGE = 4.5;

    public ReachG(GuardianAC plugin) {
        super(plugin, "Reach", "G", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long timeSinceAttack = System.currentTimeMillis() - data.getLastAttackTime();
        if (timeSinceAttack < 100) {
            double dist = player.getLocation().distance(target.getLocation());
            if (dist > MAX_PROJECTILE_MELEE_RANGE) {
                handleViolation(data, player, 4.0, 10.0, 1.0);
            }
        }
    }
}
