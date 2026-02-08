package com.yourdomain.guardianac.checks.movement.flight;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

/**
 * Flight (D) — Jetpack flight detection.
 * Flags upward bursts while already airborne without jump.
 */
public class FlightD extends Check {

    public FlightD(GuardianAC plugin) {
        super(plugin, "Flight", "D", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight() || player.isGliding() || player.isInsideVehicle()) return;
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) return;
        if (player.isRiptiding()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        if (!player.isOnGround() && data.getAirTicks() > 10 && deltaY > 0.42) {
            handleViolation(data, player, 5.0, 10.0, 2.0);
        }
    }
}
