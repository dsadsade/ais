package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;

import java.util.List;

/**
 * Detects AutoFish — automated fishing macro that perfectly times
 * casting and reeling. Legitimate fishing has variable human reaction times.
 * AutoFish mods reel in instantly when a fish bites and recast immediately,
 * producing suspiciously consistent intervals.
 */
public class AutoFishCheck extends Check {

    private static final int MIN_SAMPLES = 5;
    private static final double MAX_CONSISTENCY = 30.0; // ms std deviation — too consistent
    private static final long MIN_REACTION_TIME = 50; // ms — inhuman reaction

    public AutoFishCheck(GuardianAC plugin) {
        super(plugin, "AutoFish", CheckType.PLAYER);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();

        if (event.getState() == PlayerFishEvent.State.FISHING) {
            // Cast event
            data.addFishCastTime(now);
        } else if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            // Reel event
            data.addFishReelTime(now);

            List<Long> casts = data.getFishCastTimes();
            List<Long> reels = data.getFishReelTimes();

            if (reels.size() >= MIN_SAMPLES && casts.size() >= MIN_SAMPLES) {
                // Check cast-to-reel timing consistency (reaction time after bite)
                // Using reel intervals for consistency check
                double[] intervals = new double[reels.size() - 1];
                for (int i = 1; i < reels.size(); i++) {
                    intervals[i - 1] = reels.get(i) - reels.get(i - 1);
                }

                double stdDev = MathUtil.standardDeviation(intervals);

                // Also check if recast is suspiciously fast
                if (!casts.isEmpty() && !reels.isEmpty()) {
                    long lastReel = reels.get(reels.size() - 1);
                    long nextCast = casts.get(casts.size() - 1);
                    long recastDelay = nextCast - lastReel;

                    if (recastDelay >= 0 && recastDelay < MIN_REACTION_TIME) {
                        handleViolation(data, player, 3.0, 12.0, 1.0);
                    }
                }

                if (stdDev < MAX_CONSISTENCY && intervals.length >= MIN_SAMPLES) {
                    handleViolation(data, player, 3.0, 12.0, 1.0);
                }
            }
        }
    }
}
