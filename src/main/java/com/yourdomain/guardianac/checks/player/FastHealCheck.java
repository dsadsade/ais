package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.List;

/**
 * Detects FastHeal — regenerating health at a faster rate than vanilla allows.
 * Vanilla health regeneration has specific tick intervals based on food level
 * and saturation. FastHeal mods bypass these timers to instantly restore health.
 * This is different from Regen check which looks at health history over time;
 * this specifically flags impossible heal event rates.
 */
public class FastHealCheck extends Check {

    private static final long MIN_HEAL_INTERVAL_MS = 400; // vanilla natural regen is every 500ms+
    private static final int MAX_FAST_HEALS = 4;
    private static final long WINDOW_MS = 3000;

    public FastHealCheck(GuardianAC plugin) {
        super(plugin, "FastHeal", CheckType.PLAYER);
    }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (isExempt(player)) return;

        // Only check natural regen and saturation regen
        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED
                && event.getRegainReason() != EntityRegainHealthEvent.RegainReason.REGEN) {
            return;
        }

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        data.addHealEventTime(now);

        List<Long> healTimes = data.getHealEventTimes();
        if (healTimes.size() < 2) return;

        // Count fast heals in window
        int fastHeals = 0;
        for (int i = healTimes.size() - 1; i > 0; i--) {
            long interval = healTimes.get(i) - healTimes.get(i - 1);
            if (now - healTimes.get(i) > WINDOW_MS) break;

            if (interval < MIN_HEAL_INTERVAL_MS) {
                fastHeals++;
            }
        }

        if (fastHeals >= MAX_FAST_HEALS) {
            handleViolation(data, player, 5.0, 8.0, 0.5);
            event.setCancelled(true);
        }
    }
}
