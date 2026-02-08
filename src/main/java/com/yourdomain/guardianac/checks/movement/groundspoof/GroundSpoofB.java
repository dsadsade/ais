package com.yourdomain.guardianac.checks.movement.groundspoof;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * GroundSpoof (B) — Ground state flickering.
 * Flags rapid toggling between on-ground and off-ground states.
 */
public class GroundSpoofB extends Check {

    public GroundSpoofB(GuardianAC plugin) {
        super(plugin, "GroundSpoof", "B", CheckType.MOVEMENT);
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

        boolean onGround = player.isOnGround();
        boolean wasOnGround = data.isLastOnGround();

        if (onGround != wasOnGround) {
            data.setInvalidGroundTicks(data.getInvalidGroundTicks() + 1);
        } else {
            data.setInvalidGroundTicks(Math.max(0, data.getInvalidGroundTicks() - 1));
        }

        if (data.getInvalidGroundTicks() > 8) {
            handleViolation(data, player, 3.0, 12.0, 1.5);
        }

        data.setLastOnGround(onGround);
    }
}
