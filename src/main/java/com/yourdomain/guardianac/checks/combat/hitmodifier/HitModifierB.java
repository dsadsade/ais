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
 * HitModifier (B) — Combo extension detection.
 * Detects players who maintain impossibly long hit combos without
 * the target recovering, indicating knockback manipulation to chain hits.
 */
public class HitModifierB extends Check {

    private static final int MAX_COMBO = 8;

    public HitModifierB(GuardianAC plugin) {
        super(plugin, "HitModifier", "B", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        int consecutiveHits = data.getConsecutiveHits();

        if (consecutiveHits > MAX_COMBO) {
            double distance = player.getLocation().distance(target.getLocation());
            if (distance < 4.0) {
                handleViolation(data, player, (consecutiveHits - MAX_COMBO) * 1.5, 15.0, 1.0);
            }
        }
    }
}
