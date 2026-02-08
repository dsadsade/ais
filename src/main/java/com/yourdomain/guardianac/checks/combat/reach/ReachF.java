package com.yourdomain.guardianac.checks.combat.reach;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.PingUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Reach (F) — Moving attacker reach check.
 * Adjusts reach calculations based on attacker velocity to compensate
 * for interpolation, then flags genuine reach violations.
 */
public class ReachF extends Check {

    private static final double BASE_MAX_REACH = 3.1;
    private static final double SPEED_REACH_BONUS = 0.3;

    public ReachF(GuardianAC plugin) {
        super(plugin, "Reach", "F", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double distance = player.getEyeLocation().distance(
                target.getLocation().add(0, target.getHeight() / 2.0, 0));

        double speed = player.getVelocity().length();
        double speedBonus = Math.min(speed * SPEED_REACH_BONUS, 0.5);
        double maxReach = BASE_MAX_REACH + PingUtil.getReachBuffer(player) + speedBonus;

        if (distance > maxReach && speed > 0.15) {
            handleViolation(data, player, (distance - maxReach) * 4.0, 10.0, 0.5);
        }
    }
}
