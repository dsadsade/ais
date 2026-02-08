package com.yourdomain.guardianac.checks.movement.gravityoverride;

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
 * GravityOverride (A) — Reduced gravity.
 * Flags falling slower than vanilla gravity allows.
 */
public class GravityOverrideA extends Check {

    private static final double MIN_FALL_RATE = 0.05;

    public GravityOverrideA(GuardianAC plugin) {
        super(plugin, "GravityOverride", "A", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight() || player.isGliding() || player.isInsideVehicle()) return;
        if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING) || player.hasPotionEffect(PotionEffectType.LEVITATION)) return;
        if (player.isSwimming() || player.isRiptiding()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        if (!player.isOnGround() && data.getAirTicks() > 10 && deltaY < 0 && deltaY > -MIN_FALL_RATE) {
            handleViolation(data, player, 3.0, 12.0, 1.5);
        }
    }
}
