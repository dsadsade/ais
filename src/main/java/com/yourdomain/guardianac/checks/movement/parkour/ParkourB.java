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
 * Parkour (B) — Impossible gap crossing.
 * Flags horizontal distances in a single jump that exceed vanilla maximum.
 */
public class ParkourB extends Check {

    private static final double MAX_JUMP_DISTANCE = 4.5;

    public ParkourB(GuardianAC plugin) {
        super(plugin, "Parkour", "B", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isInsideVehicle() || player.isGliding()) return;
        if (player.isRiptiding()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (player.isOnGround() && data.getJumpStartLocation() != null) {
            double jumpDist = data.getJumpStartLocation().distance(event.getTo());
            if (jumpDist > MAX_JUMP_DISTANCE && data.getAirTicks() > 3) {
                handleViolation(data, player, 4.0, 10.0, 2.0);
            }
        }

        if (!player.isOnGround() && data.getAirTicks() == 0) {
            data.setJumpStartLocation(event.getFrom().clone());
        }
    }
}
