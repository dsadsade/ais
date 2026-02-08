package com.yourdomain.guardianac.checks.movement.parkour;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Parkour (A) — Perfect jump timing on every block.
 * Flags players who consistently land on 1-block targets with zero error.
 */
public class ParkourA extends Check {

    private int perfectLandCount;

    public ParkourA(GuardianAC plugin) {
        super(plugin, "Parkour", "A", CheckType.MOVEMENT);
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

        boolean wasAir = data.getAirTicks() > 5;

        if (player.isOnGround() && wasAir) {
            double posX = event.getTo().getX() - Math.floor(event.getTo().getX());
            double posZ = event.getTo().getZ() - Math.floor(event.getTo().getZ());
            // Landing near center of block consistently
            if (Math.abs(posX - 0.5) < 0.15 && Math.abs(posZ - 0.5) < 0.15) {
                perfectLandCount++;
                if (perfectLandCount > 6) {
                    handleViolation(data, player, 2.0, 15.0, 1.0);
                }
            }
        }

        if (player.isOnGround()) {
            perfectLandCount = Math.max(0, perfectLandCount - 1);
        }
    }
}
