package com.yourdomain.guardianac.checks.combat.killaura;

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
 * KillAura (P) — Attack range variance consistency.
 * Flags players whose attack distances have extremely low variance,
 * indicating automated distance maintenance (too consistent = bot).
 */
public class KillAuraP extends Check {

    private static final int MIN_SAMPLES = 10;
    private static final double MIN_VARIANCE_THRESHOLD = 0.01;

    public KillAuraP(GuardianAC plugin) {
        super(plugin, "KillAura", "P", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double dist = player.getLocation().distance(target.getLocation());
        data.addReachDistance(dist);

        List<Double> distances = data.getRecentReachDistances();
        if (distances.size() < MIN_SAMPLES) return;

        double variance = MathUtil.variance(distances);
        double mean = MathUtil.mean(distances);

        if (variance < MIN_VARIANCE_THRESHOLD && mean > 2.5) {
            handleViolation(data, player, 4.0, 12.0, 1.0);
        }
    }
}
