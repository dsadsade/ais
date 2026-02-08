package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Detects FastClimb — climbing ladders/vines faster than vanilla allows.
 * Vanilla ladder climbing speed is ~0.12 blocks/tick. FastClimb mods
 * increase this to 0.2+ blocks/tick, allowing near-instant ladder traversal.
 */
public class FastClimbCheck extends Check {

    private static final double MAX_CLIMB_SPEED = 0.16; // blocks/tick, vanilla is ~0.12
    private static final int MAX_FLAGS = 5;

    public FastClimbCheck(GuardianAC plugin) {
        super(plugin, "FastClimb", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Check if on a climbable block
        Material feetMat = player.getLocation().getBlock().getType();
        boolean isClimbing = feetMat == Material.LADDER || feetMat == Material.VINE
                || feetMat == Material.TWISTING_VINES || feetMat == Material.WEEPING_VINES
                || feetMat == Material.CAVE_VINES || feetMat == Material.SCAFFOLDING;

        if (!isClimbing) {
            data.setClimbSpeedFlags(Math.max(0, data.getClimbSpeedFlags() - 1));
            return;
        }

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        // Only check upward movement on ladders
        if (deltaY <= 0) return;

        data.setClimbSpeed(deltaY);

        if (deltaY > MAX_CLIMB_SPEED) {
            data.setClimbSpeedFlags(data.getClimbSpeedFlags() + 1);

            if (data.getClimbSpeedFlags() >= MAX_FLAGS) {
                handleViolation(data, player, 4.0, 10.0, 1.0);
                data.setClimbSpeedFlags(0);
            }
        } else {
            data.setClimbSpeedFlags(Math.max(0, data.getClimbSpeedFlags() - 1));
        }
    }
}
