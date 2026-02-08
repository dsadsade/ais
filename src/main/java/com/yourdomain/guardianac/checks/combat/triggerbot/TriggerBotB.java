package com.yourdomain.guardianac.checks.combat.triggerbot;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * TriggerBot (B) — Distance-based trigger detection.
 * Detects attacks that consistently fire exactly when a target
 * enters maximum reach distance.
 */
public class TriggerBotB extends Check {

    private static final double REACH_TRIGGER_DISTANCE = 2.9;
    private static final double REACH_TOLERANCE = 0.15;

    public TriggerBotB(GuardianAC plugin) {
        super(plugin, "TriggerBot", "B", CheckType.COMBAT);
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
                target.getBoundingBox().getCenter().toLocation(player.getWorld()));

        if (Math.abs(distance - REACH_TRIGGER_DISTANCE) < REACH_TOLERANCE) {
            data.addRecentReachDistance(distance);

            java.util.List<Double> recent = data.getRecentReachDistances();
            if (recent.size() >= 5) {
                long closeToThreshold = recent.stream()
                        .filter(d -> Math.abs(d - REACH_TRIGGER_DISTANCE) < REACH_TOLERANCE)
                        .count();
                if (closeToThreshold >= 4) {
                    handleViolation(data, player, 4.0, 10.0, 1.0);
                }
            }
        }
    }
}
