package com.yourdomain.guardianac.checks.movement.safewalk;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * SafeWalk (A) — Edge sneak detection.
 * Flags players always stopping precisely at block edges.
 */
public class SafeWalkA extends Check {

    private int edgeStopCount;

    public SafeWalkA(GuardianAC plugin) {
        super(plugin, "SafeWalk", "A", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle() || player.isFlying()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location to = event.getTo();
        double edgeX = to.getX() - Math.floor(to.getX());
        double edgeZ = to.getZ() - Math.floor(to.getZ());

        boolean atEdge = edgeX < 0.3 || edgeX > 0.7 || edgeZ < 0.3 || edgeZ > 0.7;
        Location below = to.clone().subtract(0, 1, 0);

        if (atEdge && below.getBlock().getType() != Material.AIR && player.isSneaking()) {
            edgeStopCount++;
            if (edgeStopCount > 10) {
                handleViolation(data, player, 2.0, 15.0, 1.0);
            }
        } else {
            edgeStopCount = Math.max(0, edgeStopCount - 1);
        }
    }
}
