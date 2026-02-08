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
 * Reach (H) — Consistent max reach detection.
 * Flags players who consistently attack at maximum allowed distance.
 * Always-max-reach indicates an automated reach hack.
 */
public class ReachH extends Check {

    private static final int MIN_SAMPLES = 10;
    private static final double HIGH_REACH_THRESHOLD = 2.8;

    public ReachH(GuardianAC plugin) {
        super(plugin, "Reach", "H", CheckType.COMBAT);
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

        long highReachCount = distances.stream()
                .filter(d -> d > HIGH_REACH_THRESHOLD).count();

        double ratio = (double) highReachCount / distances.size();
        if (ratio > 0.8) {
            handleViolation(data, player, 4.0, 12.0, 1.0);
        }
    }
}
