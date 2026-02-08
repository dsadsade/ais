package com.yourdomain.guardianac.checks.combat.fightbot;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * FightBot (A) — Combat automation detection.
 * Tracks composite combat action score (aim, timing, movement together)
 * to detect fully automated combat routines.
 */
public class FightBotA extends Check {

    private static final double ACTION_SCORE_THRESHOLD = 8.0;

    public FightBotA(GuardianAC plugin) {
        super(plugin, "FightBot", "A", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double score = 0;

        if (player.isSprinting()) score += 1.0;
        if (data.wasBlocking()) score += 1.5;

        long attackDelta = System.currentTimeMillis() - data.getLastAttackTime();
        if (attackDelta > 0 && attackDelta < 100) score += 2.0;

        float yawDelta = Math.abs(player.getLocation().getYaw() - data.getLastYaw());
        if (yawDelta > 180) yawDelta = 360 - yawDelta;
        if (yawDelta > 30) score += 1.5;

        if (data.getConsecutiveHits() > 3) score += 2.0;

        data.setCombatActionScore(data.getCombatActionScore() + score);

        if (data.getCombatActionScore() > ACTION_SCORE_THRESHOLD) {
            handleViolation(data, player, 4.0, 15.0, 2.0);
            data.setCombatActionScore(0);
        }
    }
}
