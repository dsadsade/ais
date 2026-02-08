package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;

import java.util.List;

/**
 * Detects AutoTool hacks — instantly switching to the optimal tool before breaking a block.
 * Legitimate players switch tools manually with scroll or hotkeys.
 * AutoTool mods switch tools within 1-2 ticks before every block break,
 * producing an inhuman pattern of rapid slot changes.
 */
public class AutoToolCheck extends Check {

    private static final long MIN_SWITCH_INTERVAL_MS = 30; // absolute minimum between switches
    private static final int MAX_SWITCHES_PER_WINDOW = 8;
    private static final long WINDOW_MS = 2000;

    public AutoToolCheck(GuardianAC plugin) {
        super(plugin, "AutoTool", CheckType.PLAYER);
    }

    @EventHandler
    public void onHeldItemChange(PlayerItemHeldEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        data.addToolSwitchTime(now);

        List<Long> switchTimes = data.getToolSwitchTimes();
        if (switchTimes.size() < 2) return;

        // Check interval between consecutive switches
        long interval = now - switchTimes.get(switchTimes.size() - 2);
        if (interval < MIN_SWITCH_INTERVAL_MS) {
            handleViolation(data, player, 3.0, 10.0, 1.5);
            return;
        }

        // Count switches within window
        long windowStart = now - WINDOW_MS;
        int count = 0;
        for (long t : switchTimes) {
            if (t >= windowStart) count++;
        }

        if (count >= MAX_SWITCHES_PER_WINDOW) {
            handleViolation(data, player, 3.0, 10.0, 1.0);
        }
    }
}
