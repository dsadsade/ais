package com.yourdomain.guardianac.checks.combat.killaura;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * KillAura (L) — Detects attacks during item consumption.
 * Vanilla clients cannot attack while eating, drinking, or using items.
 */
public class KillAuraL extends Check {

    public KillAuraL(GuardianAC plugin) {
        super(plugin, "KillAura", "L", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (data.isUsingItem()) {
            long useDuration = System.currentTimeMillis() - data.getItemUseStartTime();
            if (useDuration > 200) {
                handleViolation(data, player, 6.0, 10.0, 1.0);
            }
        }
    }
}
