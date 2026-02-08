package com.yourdomain.guardianac.checks.combat.aimbot;

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

import java.util.List;

/**
 * Aimbot (G) — Backtrack aim (aiming at old positions).
 * Detects when a player aims at a target's previous position rather
 * than current, indicating backtrack/lag-compensation exploits.
 */
public class AimbotG extends Check {

    private static final double OLD_POS_ANGLE_THRESHOLD = 15.0;

    public AimbotG(GuardianAC plugin) {
        super(plugin, "Aimbot", "G", CheckType.COMBAT);
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
        Vector toCurrent = target.getLocation().toVector()
                .subtract(player.getEyeLocation().toVector()).normalize();
        double currentAngle = Math.toDegrees(lookDir.angle(toCurrent));

        List<Location> oldPositions = data.getRecentAttackLocations();
        if (oldPositions.size() < 3) {
            data.addRecentAttackLocation(target.getLocation());
            return;
        }

        for (Location oldLoc : oldPositions) {
            if (oldLoc.getWorld() != player.getWorld()) continue;
            Vector toOld = oldLoc.toVector().subtract(player.getEyeLocation().toVector()).normalize();
            double oldAngle = Math.toDegrees(lookDir.angle(toOld));

            if (oldAngle < currentAngle - OLD_POS_ANGLE_THRESHOLD) {
                handleViolation(data, player, 4.0, 12.0, 1.0);
                break;
            }
        }

        data.addRecentAttackLocation(target.getLocation());
    }
}
