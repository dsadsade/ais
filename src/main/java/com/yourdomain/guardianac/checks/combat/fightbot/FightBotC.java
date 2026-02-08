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
 * FightBot (C) — Combo execution pattern detection.
 * Detects automated combo patterns: perfect sprint-hit-wtap-repeat
 * that execute identically every time.
 */
public class FightBotC extends Check {

    private static final int PERFECT_COMBO_THRESHOLD = 5;

    public FightBotC(GuardianAC plugin) {
        super(plugin, "FightBot", "C", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        boolean sprinting = player.isSprinting();
        boolean wasSprinting = data.wasSprinting();

        boolean perfectComboTick = sprinting && !wasSprinting
                && data.getConsecutiveHits() > 0;

        if (perfectComboTick) {
            data.setPerfectSprintResets(data.getPerfectSprintResets() + 1);
        }

        if (data.getConsecutiveHits() == 0) {
            if (data.getPerfectSprintResets() >= PERFECT_COMBO_THRESHOLD) {
                handleViolation(data, player, 5.0, 12.0, 1.0);
            }
            data.setPerfectSprintResets(0);
        }
    }
}
