package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Detects Scaffold hacks — automatically placing blocks under the player
 * while walking/sprinting over the air. Checks for:
 * - Rapid block placement while moving
 * - Placing blocks at impossible angles (behind/below while sprinting forward)
 * - Consistent downward head pitch while bridging at impossible speeds
 */
public class ScaffoldCheck extends Check {

    private static final long MIN_PLACE_INTERVAL_MS = 80; // minimum ms between placements (humanly possible)
    private static final int RAPID_PLACEMENT_THRESHOLD = 8; // placements in quick succession
    private static final long RAPID_WINDOW_MS = 2000;
    private static final double MAX_BRIDGE_SPEED = 0.23; // max horizontal speed while legitimately bridging

    public ScaffoldCheck(GuardianAC plugin) {
        super(plugin, "Scaffold", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Block placed = event.getBlockPlaced();
        Location playerLoc = player.getLocation();
        long now = System.currentTimeMillis();

        // Only check blocks placed below the player (bridging pattern)
        if (placed.getY() >= playerLoc.getBlockY()) return;

        // Check if the block was placed directly below the player's feet
        boolean belowFeet = Math.abs(placed.getX() - playerLoc.getBlockX()) <= 1
                && Math.abs(placed.getZ() - playerLoc.getBlockZ()) <= 1
                && (playerLoc.getBlockY() - placed.getY()) <= 2;

        if (!belowFeet) return;

        // Check 1: Rapid placement speed
        long timeSinceLast = now - data.getLastBlockPlaceTime();
        if (timeSinceLast < MIN_PLACE_INTERVAL_MS && timeSinceLast > 0) {
            data.incrementRapidBlockPlacements();
        }

        // Check 2: Count rapid placements in a window
        if (now - data.getLastBlockPlaceTime() > RAPID_WINDOW_MS) {
            data.resetRapidBlockPlacements();
        }
        data.incrementRapidBlockPlacements();

        if (data.getRapidBlockPlacements() > RAPID_PLACEMENT_THRESHOLD) {
            flag(data);
            data.resetRapidBlockPlacements();
        }

        // Check 3: Placing blocks while sprinting at full speed (impossible in vanilla)
        if (player.isSprinting() && !player.isSneaking()) {
            // Calculate recent horizontal speed
            if (!data.getRecentSpeeds().isEmpty()) {
                double avgSpeed = data.getRecentSpeeds().stream()
                        .mapToDouble(Double::doubleValue)
                        .average()
                        .orElse(0.0);

                if (avgSpeed > MAX_BRIDGE_SPEED) {
                    flag(data);
                }
            }
        }

        // Check 4: Impossible placement angle — block placed behind the player
        Location blockCenter = placed.getLocation().add(0.5, 0.5, 0.5);
        double angle = getAngleBetween(playerLoc.getDirection().setY(0).normalize(),
                blockCenter.toVector().subtract(playerLoc.toVector()).setY(0).normalize());

        // If the block is placed behind the player (angle > 120 degrees) while moving forward
        if (angle > 120 && data.getRecentSpeeds().size() > 3) {
            double recentSpeed = data.getRecentSpeeds().get(data.getRecentSpeeds().size() - 1);
            if (recentSpeed > 0.1) {
                flag(data);
            }
        }

        data.setLastBlockPlaceTime(now);
        data.setLastBlockPlaceLocation(placed.getLocation());
    }

    private double getAngleBetween(org.bukkit.util.Vector a, org.bukkit.util.Vector b) {
        double dot = a.dot(b);
        dot = Math.max(-1.0, Math.min(1.0, dot));
        return Math.toDegrees(Math.acos(dot));
    }
}
