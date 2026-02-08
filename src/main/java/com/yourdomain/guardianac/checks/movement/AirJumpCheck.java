package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Detects AirJump — jumping multiple times while airborne.
 * Vanilla Minecraft only allows a single jump from ground level.
 * AirJump/MultiJump hacks allow the player to jump again at the apex
 * of each jump, effectively flying upward in steps.
 */
public class AirJumpCheck extends Check {

    private static final double JUMP_VELOCITY = 0.42; // vanilla jump velocity
    private static final double JUMP_TOLERANCE = 0.1;
    private static final int MAX_AIR_JUMPS = 1; // one air "jump" could be lag, 2+ = cheat

    public AirJumpCheck(GuardianAC plugin) {
        super(plugin, "AirJump", CheckType.MOVEMENT);
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
        boolean onGround = player.isOnGround();

        if (onGround) {
            data.setAirJumpCount(0);
            data.setLastJumpY(event.getTo().getY());
            return;
        }

        // Check for a new upward impulse while airborne (characteristic of double jump)
        double lastDY = data.getLastDeltaY();
        // Jump detection: deltaY is positive and close to jump velocity, while previous was negative (falling)
        if (deltaY > JUMP_VELOCITY - JUMP_TOLERANCE && lastDY < -0.01) {
            // Check no blocks above — not a ceiling bounce
            Block above = player.getLocation().add(0, 2, 0).getBlock();
            if (above.getType() == Material.AIR) {
                data.setAirJumpCount(data.getAirJumpCount() + 1);

                if (data.getAirJumpCount() > MAX_AIR_JUMPS) {
                    handleViolation(data, player, 6.0, 8.0, 0.5);
                }
            }
        }
    }
}
