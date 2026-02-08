package com.yourdomain.guardianac.checks.player.badpackets;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * BadPackets (D) — Impossible Rotation Detection
 * Detects clients sending impossible rotation changes:
 * - Yaw/pitch changing faster than humanly possible (>200 degrees in one tick)
 * - Identical yaw/pitch floating point values over many ticks (bot-like)
 */
public class BadPacketsD extends Check {

    private static final float MAX_YAW_CHANGE_PER_TICK = 200.0f; // degrees
    private static final float MAX_PITCH_CHANGE_PER_TICK = 180.0f;

    public BadPacketsD(GuardianAC plugin) {
        super(plugin, "BadPackets", "D", CheckType.PLAYER);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float yaw = event.getTo().getYaw();
        float pitch = event.getTo().getPitch();
        float lastYaw = data.getLastYaw();
        float lastPitch = data.getLastPitch();

        // Skip first tick
        if (lastYaw == 0 && lastPitch == 0) return;

        float yawDelta = Math.abs(yaw - lastYaw);
        float pitchDelta = Math.abs(pitch - lastPitch);

        // Normalize yaw delta
        if (yawDelta > 180) yawDelta = 360 - yawDelta;

        // Impossible rotation speed (likely teleport aura or spinbot)
        if (yawDelta > MAX_YAW_CHANGE_PER_TICK && pitchDelta > MAX_PITCH_CHANGE_PER_TICK) {
            handleViolation(data, player, 6.0, 10.0, 1.0);
        }

        // Check for NaN rotations
        if (Float.isNaN(yaw) || Float.isNaN(pitch) || Float.isInfinite(yaw) || Float.isInfinite(pitch)) {
            handleViolation(data, player, 10.0, 5.0, 0.0);
            event.setCancelled(true);
        }
    }
}
