package com.yourdomain.guardianac.checks.combat.aimassist;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.PingUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * AimAssist (B) — Snap Correction Detection
 * Detects rapid aim corrections (snapping) toward the target in a short window.
 * Legitimate players don't have frequent perfectly-timed snaps.
 * Aim assist mods often snap to the target then resume normal movement.
 */
public class AimAssistB extends Check {

    private static final float SNAP_THRESHOLD = 15.0f; // degrees of sudden correction
    private static final int MAX_SNAPS_PER_WINDOW = 4;
    private static final long SNAP_WINDOW_MS = 3000; // 3 seconds

    public AimAssistB(GuardianAC plugin) {
        super(plugin, "AimAssist", "B", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location loc = player.getLocation();
        float yaw = loc.getYaw();

        float yawDelta = Math.abs(yaw - data.getLastYaw());
        if (yawDelta > 180) yawDelta = 360 - yawDelta;

        double threshold = SNAP_THRESHOLD * PingUtil.getRotationMultiplier(player);

        // Detect snaps: large rotation deltas followed by a hit
        if (yawDelta > threshold) {
            long now = System.currentTimeMillis();

            if (now - data.getSnapCorrectionWindowStart() > SNAP_WINDOW_MS) {
                data.resetSnapCorrectionCount();
                data.setSnapCorrectionWindowStart(now);
            }

            data.incrementSnapCorrectionCount();

            if (data.getSnapCorrectionCount() >= MAX_SNAPS_PER_WINDOW) {
                handleViolation(data, player, 5.0, 12.0, 1.0);
                data.resetSnapCorrectionCount();
            }
        }
    }
}
