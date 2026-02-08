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
 * KillAura (K) — Detects attacking while inventory is open.
 * Vanilla clients cannot attack entities while an inventory window is displayed.
 */
public class KillAuraK extends Check {

    public KillAuraK(GuardianAC plugin) {
        super(plugin, "KillAura", "K", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (data.isInventoryOpen()) {
            long openDuration = System.currentTimeMillis() - data.getInventoryOpenTime();
            if (openDuration > 100) {
                handleViolation(data, player, 8.0, 12.0, 1.0);
            }
        }
    }
}
