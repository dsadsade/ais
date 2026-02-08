package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Detects AntiVoid hacks — players preventing void death by teleporting back up
 * or stopping their fall below Y=0. Legitimate players should fall continuously
 * once below the build limit with no blocks beneath them.
 */
public class AntiVoidCheck extends Check {

    private static final double VOID_Y_LEVEL = -10.0;
    private static final double MIN_FALL_RATE = -0.3; // should be falling at least this fast

    public AntiVoidCheck(GuardianAC plugin) {
        super(plugin, "AntiVoid", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isInsideVehicle() || player.isGliding()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        double deltaY = to.getY() - from.getY();

        // Track lowest Y during a fall
        if (to.getY() < data.getLowestY()) {
            data.setLowestY(to.getY());
        }

        // Reset when on ground
        if (player.isOnGround()) {
            data.setLowestY(to.getY());
            return;
        }

        // Check: Player was in the void area and suddenly moved up
        if (data.getLowestY() < VOID_Y_LEVEL && deltaY > 0 && to.getY() < 0) {
            // Moving upward while in the void region is suspicious
            handleViolation(data, player, 8.0, 10.0, 0.5);
        }

        // Check: Player is below void and not falling fast enough
        if (to.getY() < VOID_Y_LEVEL && deltaY > MIN_FALL_RATE && data.getAirTicks() > 10) {
            handleViolation(data, player, 5.0, 10.0, 1.0);
        }
    }
}
