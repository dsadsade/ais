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
 * KillAura (V) — Invalid attack entity ID detection.
 * Detects attacking invalid or already dead entities.
 */
public class KillAuraV extends Check {

    public KillAuraV(GuardianAC plugin) {
        super(plugin, "KillAura", "V", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (target.isDead() || !target.isValid()) {
            handleViolation(data, player, 10.0, 8.0, 0.5);
            return;
        }

        double distance = player.getLocation().distance(target.getLocation());
        if (distance > 64.0) {
            handleViolation(data, player, 8.0, 8.0, 0.5);
        }
    }
}
