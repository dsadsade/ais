package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Detects SkinBlinker — rapidly toggling skin layers to create a flashing effect.
 * Blinker mods toggle the player's skin layers every tick, creating a strobe-like
 * visual. While not gameplay-breaking, it's often paired with other hacks and can
 * cause client-side lag for nearby players. Detected via rapid rotation combined
 * with metadata changes (approximated by detecting the derp-like pattern with
 * pitch oscillation to extreme values).
 */
public class SkinBlinkerCheck extends Check {

    private static final float EXTREME_PITCH_THRESHOLD = 85.0f;
    private static final int MAX_OSCILLATIONS = 10;
    private static final long WINDOW_MS = 2000;

    public SkinBlinkerCheck(GuardianAC plugin) {
        super(plugin, "SkinBlinker", CheckType.PLAYER);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float pitch = event.getTo().getPitch();
        float lastPitch = event.getFrom().getPitch();
        long now = System.currentTimeMillis();

        // Detect extreme pitch oscillation (snapping between +90 and -90)
        boolean extremeUp = pitch > EXTREME_PITCH_THRESHOLD && lastPitch < -EXTREME_PITCH_THRESHOLD;
        boolean extremeDown = pitch < -EXTREME_PITCH_THRESHOLD && lastPitch > EXTREME_PITCH_THRESHOLD;

        if (extremeUp || extremeDown) {
            if (now - data.getSkinChangeWindowStart() > WINDOW_MS) {
                data.setSkinChangeCount(1);
                data.setSkinChangeWindowStart(now);
            } else {
                data.setSkinChangeCount(data.getSkinChangeCount() + 1);
            }

            if (data.getSkinChangeCount() >= MAX_OSCILLATIONS) {
                handleViolation(data, player, 3.0, 10.0, 0.5);
                data.setSkinChangeCount(0);
            }
        }
    }
}
