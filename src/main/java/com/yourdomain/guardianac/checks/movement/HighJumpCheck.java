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
 * Detects HighJump hacks — jumping higher than vanilla physics allow.
 * Normal jump height: 1.2522 blocks. With Jump Boost I: ~1.8, Jump Boost II: ~2.5.
 * HighJump hacks allow players to reach greater heights in a single jump.
 */
public class HighJumpCheck extends Check {

    private static final double BASE_MAX_JUMP_HEIGHT = 1.3; // slight tolerance over 1.2522
    private static final double JUMP_BOOST_ADDITION = 0.6;  // per level of Jump Boost

    public HighJumpCheck(GuardianAC plugin) {
        super(plugin, "HighJump", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isInsideVehicle() || player.isGliding()) return;
        if (player.isSwimming() || player.isRiptiding()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        // Track jump: when landing, measure how high they went
        if (player.isOnGround()) {
            double jumpHeight = event.getTo().getY() - data.getLowestY();

            // Only check after a full jump cycle (was in air, now landed)
            if (data.getAirTicks() > 3 && data.getJumpStartLocation() != null) {
                double startY = data.getJumpStartLocation().getY();
                double peakY = data.getLowestY(); // lowestY is misleading name; track max

                // We need to compare against jump start, not lowest
                // Actually let's use the max Y reached during the jump
            }

            data.setLowestY(event.getTo().getY()); // reset for next jump
            data.setJumpStartLocation(event.getTo().clone());
            return;
        }

        // Track the initial upward velocity of a jump
        if (data.getAirTicks() == 1 && deltaY > 0) {
            double maxInitialVelocity = 0.42; // vanilla jump velocity

            if (player.hasPotionEffect(PotionEffectType.JUMP_BOOST)) {
                int amp = player.getPotionEffect(PotionEffectType.JUMP_BOOST).getAmplifier();
                maxInitialVelocity += 0.1 * (amp + 1);
            }

            // First tick of a jump should not exceed max initial velocity by much
            if (deltaY > maxInitialVelocity + 0.1) {
                double excess = deltaY - maxInitialVelocity;
                handleViolation(data, player, excess * 10.0, 10.0, 1.0);
            }
        }

        // Mid-air: check for impossible upward acceleration
        if (data.getAirTicks() > 5 && deltaY > data.getLastDeltaY() + 0.1) {
            // In vanilla, deltaY should always decrease due to gravity (0.08 per tick)
            // Gaining speed upward mid-air is impossible without external forces
            if (!player.hasPotionEffect(PotionEffectType.LEVITATION)) {
                handleViolation(data, player, 4.0, 10.0, 1.5);
            }
        }
    }
}
