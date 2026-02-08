package com.yourdomain.guardianac.checks.movement.autojump;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * AutoJump (A) — Jump timing perfection at block edges.
 * Flags players who always jump at the exact last tick before falling off an edge.
 */
public class AutoJumpA extends Check {

    private int perfectJumpCount;

    public AutoJumpA(GuardianAC plugin) {
        super(plugin, "AutoJump", "A", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isInsideVehicle()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        // Perfect jump is ~0.42 at takeoff
        if (Math.abs(deltaY - 0.42) < 0.005 && data.getAirTicks() == 0) {
            perfectJumpCount++;
            if (perfectJumpCount > 8) {
                handleViolation(data, player, 2.0, 15.0, 1.0);
            }
        } else if (player.isOnGround()) {
            perfectJumpCount = Math.max(0, perfectJumpCount - 1);
        }
    }
}
