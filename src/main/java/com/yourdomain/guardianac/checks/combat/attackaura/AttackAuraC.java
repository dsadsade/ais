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
 * AttackAura (C) — Sequential target cycle detection.
 * Detects repeated exact ordering: A → B → C → A → B → C,
 * indicating a predictable round-robin target cycle.
 */
public class AttackAuraC extends Check {

    private static final int MIN_CYCLE_LENGTH = 6;

    public AttackAuraC(GuardianAC plugin) {
        super(plugin, "AttackAura", "C", CheckType.COMBAT);
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

        List<Integer> order = data.getTargetCycleOrder();
        if (order.size() < MIN_CYCLE_LENGTH) return;

        int size = order.size();
        for (int patLen = 2; patLen <= size / 2; patLen++) {
            boolean repeats = true;
            for (int i = 0; i < patLen; i++) {
                int a = order.get(size - 1 - i);
                int b = order.get(size - 1 - i - patLen);
                if (a != b) {
                    repeats = false;
                    break;
                }
            }
            if (repeats) {
                handleViolation(data, player, 5.0, 10.0, 1.0);
                return;
            }
        }
    }
}
