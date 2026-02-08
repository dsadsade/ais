package com.yourdomain.guardianac.checks.combat.reach;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

/**
 * Reach (J) — Statistical reach distribution (kurtosis analysis).
 * Reach hacks produce a leptokurtic distribution because most attacks
 * cluster at one extended distance.
 */
public class ReachJ extends Check {

    private static final int MIN_SAMPLES = 15;
    private static final double MAX_KURTOSIS = 6.0;

    public ReachJ(GuardianAC plugin) {
        super(plugin, "Reach", "J", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double dist = player.getEyeLocation().distance(target.getLocation());
        data.addReachDistance(dist);

        List<Double> distances = data.getRecentReachDistances();
        if (distances.size() < MIN_SAMPLES) return;

        double kurtosis = MathUtil.kurtosis(distances);
        double mean = MathUtil.mean(distances);

        if (kurtosis > MAX_KURTOSIS && mean > 2.5) {
            handleViolation(data, player, 4.0, 12.0, 1.0);
        }
    }
}
