package com.yourdomain.guardianac.checks.combat.fightbot;

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
 * FightBot (B) — Target selection algorithm detection.
 * Detects bots that repeatedly cycle through targets in a specific,
 * non-human order (e.g., nearest first, lowest HP first consistently).
 */
public class FightBotB extends Check {

    private static final int MIN_TARGETS = 4;

    public FightBotB(GuardianAC plugin) {
        super(plugin, "FightBot", "B", CheckType.COMBAT);
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
        if (cycleOrder.size() < MIN_TARGETS * 2) return;

        int patternLen = findRepeatingPatternLength(cycleOrder);
        if (patternLen > 0 && patternLen <= MIN_TARGETS) {
            handleViolation(data, player, 5.0, 12.0, 1.0);
        }
    }

    private int findRepeatingPatternLength(List<Integer> order) {
        int size = order.size();
        for (int len = 2; len <= size / 2; len++) {
            boolean matches = true;
            for (int i = 0; i < len && i + len < size; i++) {
                if (!order.get(size - 1 - i).equals(order.get(size - 1 - i - len))) {
                    matches = false;
                    break;
                }
            }
            if (matches) return len;
        }
        return -1;
    }
}
