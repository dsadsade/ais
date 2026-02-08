package com.yourdomain.guardianac.checks.movement.flight;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Flight (F) — Bounce flight detection.
 * Flags oscillating Y values (rapid up/down) while airborne.
 */
public class FlightF extends Check {

    public FlightF(GuardianAC plugin) {
        super(plugin, "Flight", "F", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight() || player.isGliding() || player.isInsideVehicle()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaY = event.getTo().getY() - event.getFrom().getY();
        double lastDY = data.getLastDeltaY();

        if (!player.isOnGround() && ((deltaY > 0.1 && lastDY < -0.1) || (deltaY < -0.1 && lastDY > 0.1))) {
            handleViolation(data, player, 4.0, 10.0, 2.0);
        }

        data.setLastDeltaY(deltaY);
    }
}
