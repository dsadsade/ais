package com.yourdomain.guardianac.checks.combat.attackaura;

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
 * AttackAura (A) — Multi-target cycling detection.
 * Detects rapid cycling between multiple targets within a short time
 * window, indicating an aura that attacks all nearby entities.
 */
public class AttackAuraA extends Check {

    private static final long CYCLE_WINDOW_MS = 500;
    private static final int TARGET_THRESHOLD = 3;

    public AttackAuraA(GuardianAC plugin) {
        super(plugin, "AttackAura", "A", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        data.addTargetCycleOrder(target.getEntityId());

        List<Integer> cycleOrder = data.getTargetCycleOrder();
        if (cycleOrder.size() < TARGET_THRESHOLD) return;

        long now = System.currentTimeMillis();
        long timeSinceFirst = now - data.getLastAttackTime();

        if (timeSinceFirst < CYCLE_WINDOW_MS) {
            long uniqueTargets = cycleOrder.stream()
                    .distinct()
                    .count();

            if (uniqueTargets >= TARGET_THRESHOLD) {
                handleViolation(data, player, 5.0, 10.0, 1.0);
            }
        }
    }
}
