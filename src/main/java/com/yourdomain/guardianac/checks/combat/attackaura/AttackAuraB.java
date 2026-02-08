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
 * AttackAura (B) — Entity sorting algorithm detection.
 * Detects when targets are always attacked in distance-sorted order,
 * indicating an automated target selection algorithm.
 */
public class AttackAuraB extends Check {

    private static final int MIN_TARGETS = 3;

    public AttackAuraB(GuardianAC plugin) {
        super(plugin, "AttackAura", "B", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        int targetId = target.getEntityId();
        int lastTargetId = data.getLastTargetEntityId();

        if (targetId != lastTargetId && lastTargetId != -1) {
            List<Integer> order = data.getTargetCycleOrder();
            if (order.size() >= MIN_TARGETS) {
                boolean alwaysSorted = true;
                for (int i = 1; i < order.size(); i++) {
                    if (order.get(i) < order.get(i - 1)) {
                        alwaysSorted = false;
                        break;
                    }
                }
                if (alwaysSorted) {
                    handleViolation(data, player, 4.0, 10.0, 1.0);
                }
            }
        }
    }
}
