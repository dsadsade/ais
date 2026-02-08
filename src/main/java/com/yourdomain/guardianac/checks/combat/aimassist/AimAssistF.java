package com.yourdomain.guardianac.checks.combat.aimassist;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Collection;

/**
 * AimAssist (F) — Target priority analysis: always hits nearest.
 * Human target selection is inconsistent; always attacking the closest
 * entity in a multi-target scenario indicates automated selection.
 */
public class AimAssistF extends Check {

    private static final int STREAK_THRESHOLD = 8;

    public AimAssistF(GuardianAC plugin) {
        super(plugin, "AimAssist", "F", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location playerLoc = player.getLocation();
        Collection<LivingEntity> nearby = playerLoc.getNearbyLivingEntities(5.0);
        nearby.remove(player);

        if (nearby.size() < 2) return;

        double minDist = Double.MAX_VALUE;
        LivingEntity nearest = null;
        for (LivingEntity entity : nearby) {
            double dist = playerLoc.distanceSquared(entity.getLocation());
            if (dist < minDist) {
                minDist = dist;
                nearest = entity;
            }
        }

        if (nearest != null && nearest.getEntityId() == target.getEntityId()) {
            data.incrementConsecutiveHits();
        } else {
            data.resetConsecutiveHits();
        }

        if (data.getConsecutiveHits() >= STREAK_THRESHOLD) {
            handleViolation(data, player, 3.0, 12.0, 1.0);
            data.resetConsecutiveHits();
        }
    }
}
