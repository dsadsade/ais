package com.yourdomain.guardianac.checks.combat.aimbot;

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
 * Aimbot (C) — Silent aim detection (attacking without facing target).
 * Detects players who attack entities outside their view angle,
 * which is impossible in the vanilla client.
 */
public class AimbotC extends Check {

    private static final double MAX_ANGLE = 60.0;

    public AimbotC(GuardianAC plugin) {
        super(plugin, "Aimbot", "C", CheckType.COMBAT);
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

        if (angle > MAX_ANGLE) {
            handleViolation(data, player, (angle - MAX_ANGLE) * 0.5, 10.0, 1.0);
        }
    }
}
