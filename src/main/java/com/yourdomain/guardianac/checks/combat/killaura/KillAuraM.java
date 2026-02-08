package com.yourdomain.guardianac.checks.combat.killaura;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * KillAura (M) — Entity tracking speed analysis.
 * Measures how fast the player's aim follows a moving target.
 * Aimbots track targets with inhuman angular velocity.
 */
public class KillAuraM extends Check {

    private static final double MAX_TRACKING_SPEED = 45.0;

    public KillAuraM(GuardianAC plugin) {
        super(plugin, "KillAura", "M", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location lastTargetLoc = data.getLastTargetLocation();
        if (lastTargetLoc == null || target.getEntityId() != data.getLastTargetEntityId()) {
            data.setLastTargetLocation(target.getLocation());
            data.setLastTargetEntityId(target.getEntityId());
            return;
        }

        Vector toOld = lastTargetLoc.toVector().subtract(player.getEyeLocation().toVector());
        Vector toNew = target.getLocation().toVector().subtract(player.getEyeLocation().toVector());
        double angle = Math.toDegrees(toOld.angle(toNew));

        float yawDelta = Math.abs(player.getLocation().getYaw() - data.getLastYaw());
        if (yawDelta > 180) yawDelta = 360 - yawDelta;

        if (yawDelta > MAX_TRACKING_SPEED && angle > 5.0 && Math.abs(yawDelta - angle) < 3.0) {
            handleViolation(data, player, 5.0, 12.0, 1.0);
        }

        data.setLastTargetLocation(target.getLocation());
    }
}
