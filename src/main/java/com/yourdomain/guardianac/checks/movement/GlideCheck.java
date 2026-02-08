package com.yourdomain.guardianac.checks.movement;

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
 * Detects Glide hack — falling slower than normal gravity.
 * Vanilla Minecraft has a fixed gravity rate: ~0.08 blocks/tick² acceleration.
 * Glide mods slow the player's fall to a constant low rate, allowing
 * controlled descent. This is detected by checking if the fall rate stays
 * suspiciously consistent and slow over multiple ticks.
 */
public class GlideCheck extends Check {

    private static final double MIN_EXPECTED_FALL_ACCEL = 0.04; // Looser than vanilla 0.08
    private static final int MAX_GLIDE_TICKS = 8;

    public GlideCheck(GuardianAC plugin) {
        super(plugin, "Glide", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isGliding() || player.isInsideVehicle()) return;
        if (player.isSwimming()) return;
        if (isExempt(player)) return;
        if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) return;
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (player.isOnGround()) {
            data.setGlideTicks(0);
            data.setLastFallRate(0);
            return;
        }

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        // Only check when falling (negative deltaY)
        if (deltaY >= 0) {
            data.setGlideTicks(0);
            data.setLastFallRate(0);
            return;
        }

        double fallRate = Math.abs(deltaY);
        double lastFallRate = data.getLastFallRate();

        // Check if fall rate is not accelerating as expected
        if (lastFallRate > 0 && fallRate > 0) {
            double acceleration = fallRate - lastFallRate;

            // Glide: nearly constant fall rate (no acceleration)
            if (Math.abs(acceleration) < MIN_EXPECTED_FALL_ACCEL && fallRate < 0.4) {
                data.setGlideTicks(data.getGlideTicks() + 1);

                if (data.getGlideTicks() >= MAX_GLIDE_TICKS) {
                    handleViolation(data, player, 4.0, 10.0, 1.0);
                    data.setGlideTicks(0);
                }
            } else {
                data.setGlideTicks(Math.max(0, data.getGlideTicks() - 1));
            }
        }

        data.setLastFallRate(fallRate);
    }
}
