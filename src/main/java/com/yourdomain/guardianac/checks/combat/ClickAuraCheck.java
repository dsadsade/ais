package com.yourdomain.guardianac.checks.combat;

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
 * Detects ClickAura — hitting entities that are behind the player or outside their FOV.
 * While KillAura C checks angle/FOV, this check specifically detects hits on entities
 * completely behind the player (>120° from look direction), which is impossible
 * in vanilla without turning around first.
 */
public class ClickAuraCheck extends Check {

    private static final double BEHIND_ANGLE_THRESHOLD = 120.0; // degrees behind player
    private static final int MAX_BEHIND_HITS = 3;
    private static final long WINDOW_MS = 5000;

    public ClickAuraCheck(GuardianAC plugin) {
        super(plugin, "ClickAura", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location playerLoc = player.getLocation();
        Location targetLoc = target.getLocation();

        // Calculate angle between player's look direction and direction to target
        Vector toTarget = targetLoc.toVector().subtract(playerLoc.toVector()).normalize();
        Vector lookDir = playerLoc.getDirection().normalize();

        double dot = lookDir.getX() * toTarget.getX() + lookDir.getZ() * toTarget.getZ();
        double angle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot))));

        if (angle > BEHIND_ANGLE_THRESHOLD) {
            long now = System.currentTimeMillis();
            if (now - data.getClickAuraWindowStart() > WINDOW_MS) {
                data.setBehindBackHitCount(0);
                data.setClickAuraWindowStart(now);
            }

            data.setBehindBackHitCount(data.getBehindBackHitCount() + 1);

            if (data.getBehindBackHitCount() >= MAX_BEHIND_HITS) {
                handleViolation(data, player, 6.0, 8.0, 0.5);
                event.setCancelled(true);
                data.setBehindBackHitCount(0);
            }
        }
    }
}
