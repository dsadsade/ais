package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Detects Derp hack — rapidly spinning the player's head at inhuman speeds.
 * Derp mods constantly change pitch/yaw to extreme random values every tick,
 * producing a spinning animation. This is detected by counting rapid large
 * rotation changes per second.
 */
public class DerpCheck extends Check {

    private static final float MIN_ROTATION_CHANGE = 50.0f; // degrees per tick
    private static final int MAX_RAPID_ROTATIONS = 15; // per second
    private static final long WINDOW_MS = 1000;

    public DerpCheck(GuardianAC plugin) {
        super(plugin, "Derp", CheckType.PLAYER);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float yawDelta = Math.abs(event.getTo().getYaw() - event.getFrom().getYaw());
        float pitchDelta = Math.abs(event.getTo().getPitch() - event.getFrom().getPitch());
        if (yawDelta > 180) yawDelta = 360 - yawDelta;

        float totalRotation = yawDelta + pitchDelta;
        long now = System.currentTimeMillis();

        if (totalRotation >= MIN_ROTATION_CHANGE) {
            if (now - data.getRotationChangeWindowStart() > WINDOW_MS) {
                data.setRotationChangeCount(1);
                data.setRotationChangeWindowStart(now);
            } else {
                data.setRotationChangeCount(data.getRotationChangeCount() + 1);
            }

            if (data.getRotationChangeCount() >= MAX_RAPID_ROTATIONS) {
                handleViolation(data, player, 5.0, 8.0, 0.5);
                data.setRotationChangeCount(0);
            }
        }
    }
}
