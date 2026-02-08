package com.yourdomain.guardianac.checks.combat.hitmodifier;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * HitModifier (A) — Consistent critical damage detection.
 * Flags players who land a statistically impossible rate of critical
 * hits, indicating automated critical-hit module.
 */
public class HitModifierA extends Check {

    private static final int CRITICAL_STREAK_THRESHOLD = 6;

    public HitModifierA(GuardianAC plugin) {
        super(plugin, "HitModifier", "A", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        boolean isCritical = !player.isOnGround()
                && player.getFallDistance() > 0
                && !player.isInsideVehicle()
                && !player.isClimbing()
                && !player.isInWater();

        if (isCritical) {
            int flags = data.getDamageModifierFlags() + 1;
            data.setDamageModifierFlags(flags);

            if (flags >= CRITICAL_STREAK_THRESHOLD) {
                handleViolation(data, player, 5.0, 10.0, 1.0);
                data.setDamageModifierFlags(0);
            }
        } else {
            data.setDamageModifierFlags(Math.max(0, data.getDamageModifierFlags() - 1));
        }
    }
}
