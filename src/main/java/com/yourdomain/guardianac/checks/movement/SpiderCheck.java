package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Detects Spider hack — climbing up walls without ladders or vines.
 * Spider mods allow the player to ascend any vertical surface as if
 * it were a ladder. This is detected by checking for upward movement
 * while adjacent to a solid block but not on a climbable block.
 */
public class SpiderCheck extends Check {

    private static final int MAX_WALL_CLIMB_TICKS = 4;
    private static final double MIN_CLIMB_SPEED = 0.1;

    public SpiderCheck(GuardianAC plugin) {
        super(plugin, "Spider", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isGliding() || player.isInsideVehicle()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        if (player.isOnGround()) {
            data.setWallClimbTicks(0);
            return;
        }

        // Check if on a climbable block (ladder, vine, scaffolding)
        Block atFeet = player.getLocation().getBlock();
        Material feetMat = atFeet.getType();
        if (feetMat == Material.LADDER || feetMat == Material.VINE
                || feetMat == Material.SCAFFOLDING || feetMat == Material.TWISTING_VINES
                || feetMat == Material.WEEPING_VINES || feetMat == Material.CAVE_VINES) {
            data.setWallClimbTicks(0);
            return;
        }

        // Check if ascending while next to a wall
        if (deltaY > MIN_CLIMB_SPEED) {
            boolean nextToWall = false;
            Block block = player.getLocation().getBlock();
            for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                if (block.getRelative(face).getType().isSolid()) {
                    nextToWall = true;
                    break;
                }
            }

            if (nextToWall) {
                data.setWallClimbTicks(data.getWallClimbTicks() + 1);

                if (data.getWallClimbTicks() > MAX_WALL_CLIMB_TICKS) {
                    handleViolation(data, player, 5.0, 10.0, 1.0);
                }
            } else {
                data.setWallClimbTicks(Math.max(0, data.getWallClimbTicks() - 1));
            }
        } else {
            data.setWallClimbTicks(Math.max(0, data.getWallClimbTicks() - 1));
        }
    }
}
