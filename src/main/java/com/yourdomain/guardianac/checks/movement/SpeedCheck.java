package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

/**
 * Detects Speed hacks by comparing player horizontal speed against
 * dynamically calculated max speed based on:
 * - Base movement speed (includes soul speed, depth strider, etc.)
 * - Sprint status
 * - Potion effects (Speed, Jump Boost)
 * - Block below (ice, soul sand)
 * - Vehicle checks
 */
public class SpeedCheck extends Check {

    // Base speeds per tick
    private static final double BASE_WALK_SPEED = 0.222;
    private static final double BASE_SPRINT_SPEED = 0.288;
    private static final double SNEAK_MULTIPLIER = 0.3;
    // Consecutive flags needed before reporting (reduces false positives from lag spikes)
    private static final int CONSECUTIVE_FLAG_THRESHOLD = 3;

    public SpeedCheck(GuardianAC plugin) {
        super(plugin, "Speed", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isInsideVehicle()) return;
        if (player.isGliding()) return; // Elytra
        if (player.isSwimming()) return;
        if (player.isRiptiding()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double speed = event.getFrom().toVector().setY(0).distance(event.getTo().toVector().setY(0));
        data.addSpeed(speed);

        // Calculate dynamic maximum speed
        double maxSpeed;

        if (player.isSneaking()) {
            maxSpeed = BASE_WALK_SPEED * SNEAK_MULTIPLIER;
        } else if (player.isSprinting()) {
            maxSpeed = BASE_SPRINT_SPEED;
        } else {
            maxSpeed = BASE_WALK_SPEED;
        }

        // Account for movement speed attribute (includes soul speed etc.)
        org.bukkit.attribute.AttributeInstance movementAttr = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (movementAttr != null) {
            double movementSpeedMultiplier = movementAttr.getValue() / 0.1;
            maxSpeed *= movementSpeedMultiplier;
        }

        // Potion effects
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int amplifier = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier();
            maxSpeed *= (1.0 + 0.2 * (amplifier + 1));
        }
        if (player.hasPotionEffect(PotionEffectType.SLOWNESS)) {
            int amplifier = player.getPotionEffect(PotionEffectType.SLOWNESS).getAmplifier();
            maxSpeed *= Math.max(0.0, 1.0 - 0.15 * (amplifier + 1));
        }
        if (player.hasPotionEffect(PotionEffectType.JUMP_BOOST)) {
            maxSpeed *= 1.2; // Jump boost slightly increases air strafe speed
        }

        // Block below effects
        Material blockBelow = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        if (blockBelow == Material.ICE || blockBelow == Material.PACKED_ICE || blockBelow == Material.BLUE_ICE) {
            maxSpeed *= 2.5; // Ice is extremely slippery
        }
        if (blockBelow == Material.SOUL_SAND || blockBelow == Material.SOUL_SOIL) {
            if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
                maxSpeed *= 0.6; // Soul sand slows you down (unless soul speed)
            }
        }
        if (blockBelow == Material.SLIME_BLOCK) {
            maxSpeed *= 1.5; // Bounce momentum
        }

        // If the player is not on the ground, increase the threshold slightly for air strafing
        if (!player.isOnGround()) {
            maxSpeed *= 1.3;
        }

        // Network lag buffer
        maxSpeed += 0.06;

        if (speed > maxSpeed) {
            data.setConsecutiveSpeedFlags(data.getConsecutiveSpeedFlags() + 1);
            if (data.getConsecutiveSpeedFlags() >= CONSECUTIVE_FLAG_THRESHOLD) {
                flag(data);
                data.setConsecutiveSpeedFlags(0);
            }
        } else {
            data.setConsecutiveSpeedFlags(Math.max(0, data.getConsecutiveSpeedFlags() - 1));
        }
    }
}
