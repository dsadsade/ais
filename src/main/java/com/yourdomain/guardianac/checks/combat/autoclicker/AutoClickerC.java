package com.yourdomain.guardianac.checks.combat.autoclicker;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * AutoClicker (C) — Double-click pattern analysis.
 * Detects auto-clickers that fire two rapid clicks (double-click) at regular intervals.
 * This pattern is distinctive of certain macro tools and drag-clicking exploits.
 */
public class AutoClickerC extends Check {

    private static final long DOUBLE_CLICK_THRESHOLD_MS = 20;
    private static final int MIN_SAMPLES = 10;

    public AutoClickerC(GuardianAC plugin) {
        super(plugin, "AutoClicker", "C", CheckType.COMBAT);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        List<Long> timestamps = data.getClickTimestamps();

        if (!timestamps.isEmpty()) {
            long lastClick = timestamps.get(timestamps.size() - 1);
            long interval = now - lastClick;
            if (interval > 0 && interval < 2000) {
                data.addClickInterval(interval);
            }
        }
        data.addClickTimestamp(now);

        List<Long> intervals = data.getClickIntervals();
        if (intervals.size() < MIN_SAMPLES) return;

        // Count double-click pairs (intervals < threshold)
        int doubleClicks = 0;
        int totalPairs = 0;
        for (int i = 0; i < intervals.size() - 1; i += 2) {
            totalPairs++;
            long fast = intervals.get(i);
            if (fast <= DOUBLE_CLICK_THRESHOLD_MS) {
                doubleClicks++;
            }
        }

        if (totalPairs < 5) return;

        double doubleClickRatio = (double) doubleClicks / totalPairs;

        // High ratio of very fast pairs = macro double-click
        if (doubleClickRatio > 0.6 && totalPairs >= 8) {
            handleViolation(data, player, 2.5, 7.0, 0.5);
        }

        // Also check for alternating fast-slow pattern (characteristic of double-click macros)
        int alternatingCount = 0;
        for (int i = 0; i < intervals.size() - 1; i++) {
            long current = intervals.get(i);
            long next = intervals.get(i + 1);
            // Pattern: very fast followed by moderate, or vice versa
            if ((current < 25 && next > 40) || (current > 40 && next < 25)) {
                alternatingCount++;
            }
        }

        double alternatingRatio = (double) alternatingCount / (intervals.size() - 1);
        if (alternatingRatio > 0.7 && intervals.size() >= 20) {
            handleViolation(data, player, 2.0, 7.0, 0.5);
        }
    }
}
