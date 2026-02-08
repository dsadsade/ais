package com.yourdomain.guardianac.checks.combat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.PingUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Detects TPAura — teleporting to targets to land hits from impossible distances.
 * TPAura mods briefly teleport the player to the target, deal damage, then teleport back.
 * This shows up as massive position changes between consecutive move packets around attacks,
 * or hits landing from distances far beyond normal reach.
 */
public class TPAuraCheck extends Check {

    private static final double MAX_TELEPORT_DISTANCE = 6.0; // blocks moved between last pos and attack
    private static final int FLAG_THRESHOLD = 3;

    public TPAuraCheck(GuardianAC plugin) {
        super(plugin, "TPAura", CheckType.COMBAT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data != null) {
            data.setLastPositionBeforeAttack(event.getFrom().clone());
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location lastPos = data.getLastPositionBeforeAttack();
        if (lastPos == null || !lastPos.getWorld().equals(player.getWorld())) return;

        // Distance the player "jumped" to land this hit
        double distanceMoved = lastPos.distance(player.getLocation());
        double distanceToTarget = player.getLocation().distance(target.getLocation());

        double maxDist = MAX_TELEPORT_DISTANCE + PingUtil.getReachBuffer(player);

        // Large teleport to hit a target
        if (distanceMoved > maxDist && distanceToTarget < 4.0) {
            data.setTpAuraFlags(data.getTpAuraFlags() + 1);

            if (data.getTpAuraFlags() >= FLAG_THRESHOLD) {
                handleViolation(data, player, 8.0, 8.0, 0.5);
                event.setCancelled(true);
                data.setTpAuraFlags(0);
            }
        } else {
            data.setTpAuraFlags(Math.max(0, data.getTpAuraFlags() - 1));
        }
    }
}
