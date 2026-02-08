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
 * AutoJump (B) — Constant jumping pattern.
 * Flags players who jump every single time they touch the ground.
 */
public class AutoJumpB extends Check {

    private int instantJumpCount;

    public AutoJumpB(GuardianAC plugin) {
        super(plugin, "AutoJump", "B", CheckType.MOVEMENT);
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
        long timeSinceGround = System.currentTimeMillis() - data.getLastOnGroundTime();

        if (deltaY > 0.40 && timeSinceGround < 60) {
            instantJumpCount++;
            if (instantJumpCount > 10) {
                handleViolation(data, player, 2.0, 15.0, 1.0);
            }
        } else if (player.isOnGround()) {
            instantJumpCount = Math.max(0, instantJumpCount - 1);
        }
    }
}
