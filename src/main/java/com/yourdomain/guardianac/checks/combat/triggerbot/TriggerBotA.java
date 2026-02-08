package com.yourdomain.guardianac.checks.combat.triggerbot;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * TriggerBot (A) — Crosshair-enter attack timing.
 * Flags when the player attacks within a suspiciously short window
 * after a target enters their crosshair.
 */
public class TriggerBotA extends Check {

    private static final double FOV_ENTRY = 5.0;
    private static final long MIN_REACTION_MS = 30;

    public TriggerBotA(GuardianAC plugin) {
        super(plugin, "TriggerBot", "A", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Vector lookDir = player.getEyeLocation().getDirection().normalize();
        Vector toTarget = target.getEyeLocation().toVector()
                .subtract(player.getEyeLocation().toVector()).normalize();
        double angle = Math.toDegrees(lookDir.angle(toTarget));

        if (angle < FOV_ENTRY) {
            int count = data.getCrosshairAttackCount() + 1;
            data.setCrosshairAttackCount(count);

            long timeSinceLastAttack = System.currentTimeMillis() - data.getLastAttackTime();
            if (timeSinceLastAttack < MIN_REACTION_MS && count >= 3) {
                handleViolation(data, player, 4.0, 10.0, 1.0);
            }
        } else {
            data.setCrosshairAttackCount(0);
        }
    }
}
