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

import java.util.List;

/**
 * Detects Backtrack hacks — attacking entities from desynced (old) positions.
 * Backtrack exploits hold outgoing packets to make the player appear in one position
 * while actually being elsewhere, then release to register hits from a "past" location.
 * This shows up as hits landing from positions the player was at seconds ago.
 */
public class BacktrackCheck extends Check {

    private static final double MAX_POSITION_VARIANCE = 8.0; // blocks between consecutive attack positions
    private static final long POSITION_FRESHNESS_MS = 2000;

    public BacktrackCheck(GuardianAC plugin) {
        super(plugin, "Backtrack", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location currentPos = player.getLocation();
        long now = System.currentTimeMillis();

        List<Location> recentPositions = data.getRecentAttackLocations();

        // Compare current attack position with recent positions
        if (!recentPositions.isEmpty()) {
            Location lastAttackPos = recentPositions.get(recentPositions.size() - 1);
            long timeSinceLastAttack = now - data.getLastAttackLocationTime();

            if (lastAttackPos.getWorld() == currentPos.getWorld() && timeSinceLastAttack < POSITION_FRESHNESS_MS) {
                double posDelta = lastAttackPos.distance(currentPos);

                // Adjust for ping — high ping players may have larger position jumps
                double maxVariance = MAX_POSITION_VARIANCE + PingUtil.getReachBuffer(player) * 3.0;

                if (posDelta > maxVariance) {
                    handleViolation(data, player, (posDelta - maxVariance) * 2.0, 12.0, 1.0);
                }
            }
        }

        // Check for attack while supposedly moving — hit distance vs actual distance
        double attackDist = player.getLocation().distance(target.getLocation());
        double maxReach = 3.5 + PingUtil.getReachBuffer(player);

        // If they hit from too far AND their position teleported, strong backtrack indicator
        if (attackDist > maxReach && recentPositions.size() >= 3) {
            handleViolation(data, player, 5.0, 10.0, 0.5);
        }

        data.addRecentAttackLocation(currentPos.clone());
        data.setLastAttackLocationTime(now);
    }
}
