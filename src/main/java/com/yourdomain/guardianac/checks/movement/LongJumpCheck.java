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
import org.bukkit.potion.PotionEffectType;

/**
 * Detects LongJump hacks — jumping significantly further than vanilla physics allow.
 * Normal max horizontal jump distance is ~4.5 blocks (5.5 with sprint + jump boost I).
 */
public class LongJumpCheck extends Check {

    private static final double MAX_JUMP_DISTANCE_SPRINT = 5.0;
    private static final double JUMP_BOOST_MULTIPLIER = 1.2;

    public LongJumpCheck(GuardianAC plugin) {
        super(plugin, "LongJump", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight() || player.isInsideVehicle()) return;
        if (player.isGliding() || player.isSwimming() || player.isRiptiding()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        // Track the jump start
        if (player.isOnGround() && data.getAirTicks() == 0) {
            data.setJumpStartLocation(player.getLocation());
            data.setJumpStartTime(System.currentTimeMillis());
            return;
        }

        // Check on landing
        if (player.isOnGround() && data.getAirTicks() > 3 && data.getJumpStartLocation() != null) {
            Location start = data.getJumpStartLocation();
            if (start.getWorld() != null && start.getWorld().equals(to.getWorld())) {
                double horizontalDist = Math.sqrt(
                        Math.pow(to.getX() - start.getX(), 2) + Math.pow(to.getZ() - start.getZ(), 2));

                double maxDist = MAX_JUMP_DISTANCE_SPRINT;

                // Jump Boost increases distance
                if (player.hasPotionEffect(PotionEffectType.JUMP_BOOST)) {
                    int amp = player.getPotionEffect(PotionEffectType.JUMP_BOOST).getAmplifier();
                    maxDist *= JUMP_BOOST_MULTIPLIER * (1 + amp * 0.3);
                }

                // Speed effect increases distance
                if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                    int amp = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier();
                    maxDist *= (1.0 + 0.2 * (amp + 1));
                }

                if (horizontalDist > maxDist) {
                    double excess = horizontalDist - maxDist;
                    handleViolation(data, player, excess * 3.0, 12.0, 1.5);
                }
            }
            data.setJumpStartLocation(null);
        }
    }
}
