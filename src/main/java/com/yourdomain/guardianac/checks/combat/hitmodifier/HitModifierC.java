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
 * HitModifier (C) — Damage amplification detection.
 * Detects when outgoing damage is consistently higher than what the
 * player's weapon and enchantments should produce.
 */
public class HitModifierC extends Check {

    private static final double MAX_DAMAGE_MULTIPLIER = 1.5;

    public HitModifierC(GuardianAC plugin) {
        super(plugin, "HitModifier", "C", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double damage = event.getFinalDamage();
        double baseDamage = event.getDamage();

        if (baseDamage <= 0) return;

        double multiplier = damage / baseDamage;

        boolean isCritical = !player.isOnGround() && player.getFallDistance() > 0;
        double maxAllowed = isCritical ? MAX_DAMAGE_MULTIPLIER * 1.5 : MAX_DAMAGE_MULTIPLIER;

        if (multiplier > maxAllowed && damage > 5.0) {
            data.setLastDamageDealt(damage);
            handleViolation(data, player, 5.0, 10.0, 1.0);
        }
    }
}
