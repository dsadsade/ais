package com.yourdomain.guardianac.checks.player.badpackets;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * BadPackets (C) — Ground Spoof Detection
 * Detects players spoofing their onGround flag.
 * Some hacked clients claim to be on ground while clearly in the air
 * to bypass fall damage, fly detection, or get critical hits.
 */
public class BadPacketsC extends Check {

    private static final int MAX_INVALID_GROUND_TICKS = 10;

    public BadPacketsC(GuardianAC plugin) {
        super(plugin, "BadPackets", "C", CheckType.PLAYER);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isInsideVehicle() || player.isGliding()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        boolean reportedOnGround = player.isOnGround();
        Location loc = player.getLocation();

        // Check if there's actually a block below the player
        boolean actuallyOnGround = isActuallyOnGround(loc);

        if (reportedOnGround && !actuallyOnGround) {
            data.incrementInvalidGroundTicks();
            if (data.getInvalidGroundTicks() >= MAX_INVALID_GROUND_TICKS) {
                handleViolation(data, player, 5.0, 12.0, 1.0);
                data.resetInvalidGroundTicks();
            }
        } else {
            data.resetInvalidGroundTicks();
        }

        data.setLastOnGround(reportedOnGround);
    }

    private boolean isActuallyOnGround(Location loc) {
        // Check blocks below the player's feet
        for (double dx = -0.3; dx <= 0.3; dx += 0.3) {
            for (double dz = -0.3; dz <= 0.3; dz += 0.3) {
                Block below = loc.clone().add(dx, -0.1, dz).getBlock();
                if (below.getType().isSolid()) return true;
                // Check for non-solid but supportive blocks
                if (below.getType() == Material.WATER || below.getType() == Material.LAVA) return true;
            }
        }
        return false;
    }
}
