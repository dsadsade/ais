package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Detects BunnyHop — maintaining or gaining speed through repeated jumps.
 * Vanilla Minecraft friction reduces speed when landing. BHop mods time jumps
 * to maintain or increase horizontal speed, resulting in progressively faster
 * movement. Detected by tracking speed history across jump cycles.
 */
public class BunnyHopCheck extends Check {

    private static final double BASE_MAX_SPEED = 0.35; // blocks/tick
    private static final int MIN_BHOP_JUMPS = 5;
    private static final double SPEED_INCREASE_THRESHOLD = 0.02; // speed gain per jump

    public BunnyHopCheck(GuardianAC plugin) {
        super(plugin, "BunnyHop", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isGliding() || player.isInsideVehicle()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();
        double speed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double deltaY = event.getTo().getY() - event.getFrom().getY();

        // Detect jump (upward movement from ground)
        if (player.isOnGround() && deltaY > 0.3) {
            data.setBhopJumpCount(data.getBhopJumpCount() + 1);
            data.addBhopSpeed(speed);
        } else if (player.isOnGround() && deltaY <= 0) {
            // On ground, not jumping — check accumulated data
            List<Double> speeds = data.getBhopSpeedHistory();
            if (data.getBhopJumpCount() >= MIN_BHOP_JUMPS && speeds.size() >= MIN_BHOP_JUMPS) {
                // Calculate max allowed speed with potion effects
                double maxSpeed = BASE_MAX_SPEED;
                PotionEffect speedEffect = player.getPotionEffect(PotionEffectType.SPEED);
                if (speedEffect != null) {
                    maxSpeed += 0.06 * (speedEffect.getAmplifier() + 1);
                }

                // Check if speed is consistently increasing (bhop acceleration)
                int increasing = 0;
                for (int i = 1; i < speeds.size(); i++) {
                    if (speeds.get(i) > speeds.get(i - 1) + SPEED_INCREASE_THRESHOLD) {
                        increasing++;
                    }
                }

                double avgSpeed = speeds.stream().mapToDouble(Double::doubleValue).average().orElse(0);

                if (increasing >= MIN_BHOP_JUMPS - 2 || avgSpeed > maxSpeed) {
                    handleViolation(data, player, 4.0, 10.0, 1.0);
                }

                data.setBhopJumpCount(0);
                speeds.clear();
            }
        }
    }
}
