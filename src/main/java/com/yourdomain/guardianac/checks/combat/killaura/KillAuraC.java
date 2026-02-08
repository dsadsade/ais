package com.yourdomain.guardianac.checks.combat.killaura;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.PingUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * KillAura (C) — Angle / FOV check.
 * Detects hitting entities that are outside the player's field of view.
 * A player normally cannot hit entities behind them.
 */
public class KillAuraC extends Check {

    public KillAuraC(GuardianAC plugin) {
        super(plugin, "KillAura", "C", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double angle = getAngleToTarget(player, target);

        // Max angle scales with ping (high-ping players may have outdated entity positions)
        double maxAngle = 90.0 + (PingUtil.isHighPing(player) ? 20.0 : 0.0);

        if (angle > maxAngle) {
            handleViolation(data, player, 3.5, 5.0, 0.25);
        } else if (angle > 80.0) {
            // Borderline — mild violation weight
            handleViolation(data, player, 0.5, 5.0, 0.25);
        }
    }

    private double getAngleToTarget(Player player, Entity target) {
        Location eyeLoc = player.getEyeLocation();
        Vector playerDir = eyeLoc.getDirection().normalize();
        Vector toTarget = target.getLocation().add(0, target.getHeight() / 2.0, 0)
                .subtract(eyeLoc).toVector().normalize();
        return Math.toDegrees(playerDir.angle(toTarget));
    }
}
