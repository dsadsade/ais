package com.yourdomain.guardianac.checks.combat.reducedkb;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * ReducedKB (A) — Partial horizontal knockback.
 * Detects when a player consistently takes less horizontal knockback
 * than expected from hits, indicating velocity reduction.
 */
public class ReducedKBA extends Check {

    private static final double HORIZONTAL_RATIO_THRESHOLD = 0.4;
    private static final int STREAK_THRESHOLD = 4;

    public ReducedKBA(GuardianAC plugin) {
        super(plugin, "ReducedKB", "A", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (!data.isPendingVelocityCheck()) return;

        double actualHorizontal = data.getKbActualHorizontal();
        double expectedX = Math.abs(data.getLastVelocityX());
        double expectedZ = Math.abs(data.getLastVelocityZ());
        double expectedHorizontal = Math.sqrt(expectedX * expectedX + expectedZ * expectedZ);

        if (expectedHorizontal < 0.05) return;

        double ratio = actualHorizontal / expectedHorizontal;

        if (ratio < HORIZONTAL_RATIO_THRESHOLD) {
            int partialCount = data.getPartialKBCount() + 1;
            data.setPartialKBCount(partialCount);

            if (partialCount >= STREAK_THRESHOLD) {
                handleViolation(data, player, 5.0, 10.0, 0.5);
            }
        } else {
            data.setPartialKBCount(Math.max(0, data.getPartialKBCount() - 1));
        }
    }
}
