package com.yourdomain.guardianac.checks.movement;

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
 * Detects Step exploits — instantly stepping up blocks without the normal jump animation.
 * Vanilla Minecraft can only step up 0.5625 blocks without jumping.
 * Step hacks allow players to climb 1+ block instantly.
 */
public class StepCheck extends Check {

    private static final double MAX_STEP_HEIGHT = 0.6; // vanilla max stepHeight
    private static final double JUMP_VELOCITY = 0.42; // vanilla initial jump velocity

    public StepCheck(GuardianAC plugin) {
        super(plugin, "Step", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isInsideVehicle()) return;
        if (player.isGliding()) return;
        if (player.isSwimming()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        double deltaY = to.getY() - from.getY();

        // Only check upward movement that's bigger than normal step but not a proper jump
        if (deltaY > MAX_STEP_HEIGHT && deltaY < 1.3) {
            // Check if we were on the ground before this step
            if (player.isOnGround() || data.getAirTicks() <= 1) {
                // A legitimate jump has a specific velocity pattern; a step hack is instant
                double lastDeltaY = data.getLastDeltaY();

                // Normal jump: first tick deltaY ~0.42; step hack: deltaY > 0.6 from ground
                if (lastDeltaY < 0.1 && deltaY > MAX_STEP_HEIGHT) {
                    // Verify there's a block at the destination to step onto
                    Block below = to.clone().subtract(0, 0.1, 0).getBlock();
                    if (below.getType().isSolid()) {
                        // Not near stairs/slabs (which allow 0.5 steps)
                        if (!isStairOrSlab(below)) {
                            handleViolation(data, player, 5.0, 10.0, 1.0);
                        }
                    }
                }
            }
        }
    }

    private boolean isStairOrSlab(Block block) {
        String name = block.getType().name();
        return name.contains("STAIR") || name.contains("SLAB") || name.contains("STEP")
                || block.getType() == Material.SNOW;
    }
}
