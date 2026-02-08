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
 * Detects InvalidSprint — sprinting in conditions where vanilla doesn't allow it.
 * Players cannot sprint while:
 * - Moving backwards
 * - Using items (eating, blocking, drawing bow)
 * - Hunger is at 6 or below (3 drumsticks)
 * - Sneaking
 * - Blinded
 */
public class InvalidSprintCheck extends Check {

    public InvalidSprintCheck(GuardianAC plugin) {
        super(plugin, "InvalidSprint", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (!player.isSprinting()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Check: sprinting while using item
        if (data.isUsingItem()) {
            // Cannot sprint while eating/drinking/blocking
            handleViolation(data, player, 4.0, 8.0, 1.0);
            return;
        }

        // Check: sprinting with low hunger (<=6 food level)
        if (player.getFoodLevel() <= 6) {
            handleViolation(data, player, 3.0, 8.0, 0.5);
            return;
        }

        // Check: sprinting while sneaking
        if (player.isSneaking()) {
            handleViolation(data, player, 5.0, 8.0, 0.5);
            return;
        }

        // Check: sprinting while blinded
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS)) {
            handleViolation(data, player, 4.0, 8.0, 1.0);
            return;
        }

        // Check: sprinting backwards (look direction vs movement direction)
        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();
        double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (horizontal > 0.1) {
            // Movement angle vs look angle
            double moveAngle = Math.toDegrees(Math.atan2(-deltaX, deltaZ));
            float lookAngle = event.getTo().getYaw();

            double angleDiff = Math.abs(((moveAngle - lookAngle + 180) % 360) - 180);

            // Moving more than 110 degrees from look direction while sprinting
            if (angleDiff > 110) {
                handleViolation(data, player, 4.0, 10.0, 1.0);
            }
        }
    }
}
