package com.yourdomain.guardianac.checks.combat.killaura;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.List;

/**
 * KillAura (T) — Perfect combo timing detection.
 * Detects exactly 0.5s (10 ticks) between hits, indicating automated timing.
 */
public class KillAuraT extends Check {

    private static final int MIN_SAMPLES = 6;
    private static final long PERFECT_INTERVAL_MS = 500;
    private static final long TOLERANCE_MS = 15;

    public KillAuraT(GuardianAC plugin) {
        super(plugin, "KillAura", "T", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long interval = now - data.getLastAttackTime();
        if (interval > 0 && interval < 2000) {
            data.addAttackInterval(interval);
        }
        data.setLastAttackTime(now);

        List<Double> intervals = data.getAttackIntervals();
        if (intervals.size() < MIN_SAMPLES) return;

        long perfectCount = intervals.stream()
                .filter(i -> Math.abs(i - PERFECT_INTERVAL_MS) < TOLERANCE_MS)
                .count();

        if (perfectCount >= MIN_SAMPLES - 1) {
            handleViolation(data, player, 5.0, 12.0, 1.0);
        }
    }
}
