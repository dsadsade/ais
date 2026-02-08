package com.yourdomain.guardianac.checks.combat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Detects impossible arm swing frequency — swinging faster than
 * the game engine allows (more than ~20 swings per second).
 */
public class SwingFrequencyCheck extends Check {

    private static final int MAX_SWINGS_PER_SECOND = 22;
    private final Map<UUID, long[]> swingTracker = new ConcurrentHashMap<>(); // [count, windowStart]

    public SwingFrequencyCheck(GuardianAC plugin) {
        super(plugin, "SwingFrequency", CheckType.COMBAT);
    }

    @EventHandler
    public void onSwing(PlayerAnimationEvent event) {
        if (!isEnabled()) return;
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        long now = System.currentTimeMillis();
        long[] tracker = swingTracker.computeIfAbsent(player.getUniqueId(), k -> new long[]{0, now});

        if (now - tracker[1] >= 1000) {
            // New window
            if (tracker[0] > MAX_SWINGS_PER_SECOND) {
                flag(player, String.format("swings/s=%d max=%d", tracker[0], MAX_SWINGS_PER_SECOND));
            }
            tracker[0] = 1;
            tracker[1] = now;
        } else {
            tracker[0]++;
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        swingTracker.remove(event.getPlayer().getUniqueId());
    }
}
