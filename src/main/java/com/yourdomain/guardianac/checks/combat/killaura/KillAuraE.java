package com.yourdomain.guardianac.checks.combat.killaura;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

/**
 * KillAura (E) — GCD (Greatest Common Divisor) pattern analysis.
 * Analyzes mouse rotation GCD to detect non-human input (aimbots use raw angles).
 * Legitimate mice produce a consistent GCD tied to their sensitivity setting.
 * Synthetic rotations have a GCD of 0 or an inconsistent GCD.
 */
public class KillAuraE extends Check {

    private static final int MIN_GCD_SAMPLES = 15;
    private static final double MIN_GCD_VALUE = 0.001;

    public KillAuraE(GuardianAC plugin) {
        super(plugin, "KillAura", "E", CheckType.COMBAT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float yaw = event.getTo().getYaw();
        float pitch = event.getTo().getPitch();
        float lastYaw = data.getLastYaw();
        float lastPitch = data.getLastPitch();

        float deltaYaw = Math.abs(yaw - lastYaw);
        float deltaPitch = Math.abs(pitch - lastPitch);

        // Only analyze significant rotations (ignore tiny jitter)
        if (deltaYaw > 0.1 && deltaPitch > 0.1) {
            double expandedYaw = (double)(deltaYaw * 16384.0f / 360.0f); // expand to mouse units
            double expandedPitch = (double)(deltaPitch * 16384.0f / 360.0f);

            if (expandedYaw > 0 && expandedPitch > 0) {
                double gcd = MathUtil.gcd(expandedYaw, expandedPitch);
                data.addGcdSample(gcd);
            }
        }

        data.updateRotation(yaw, pitch);

        List<Double> gcdSamples = data.getGcdSamples();
        if (gcdSamples.size() < MIN_GCD_SAMPLES) return;

        // Analyze GCD consistency
        double gcdStdDev = MathUtil.standardDeviation(gcdSamples);
        double gcdMean = gcdSamples.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        // If the GCD is near zero, it means the rotations aren't coming from a real mouse
        long zeroGcds = gcdSamples.stream().filter(g -> g < MIN_GCD_VALUE).count();
        double zeroRatio = (double) zeroGcds / gcdSamples.size();

        if (zeroRatio > 0.7) {
            // Most samples have near-zero GCD — synthetic input
            handleViolation(data, player, 2.0, 8.0, 0.25);
        } else if (gcdStdDev > gcdMean * 2.0 && gcdMean > 0) {
            // Wildly inconsistent GCD — possibly switching between real/fake rotations
            handleViolation(data, player, 1.0, 8.0, 0.25);
        }
    }
}
