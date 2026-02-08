package com.yourdomain.guardianac.checks.combat.autoclicker;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * AutoClicker (E) — Drag click detection.
 * Drag clicking produces rapid bursts of many clicks followed by pauses.
 * Detects sustained impossible CPS bursts (>15 in under 500ms).
 */
public class AutoClickerE extends Check {

    private static final int BURST_THRESHOLD = 15;
    private static final long BURST_WINDOW_MS = 500;

    public AutoClickerE(GuardianAC plugin) {
        super(plugin, "AutoClicker", "E", CheckType.COMBAT);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        data.addClickTimestamp(now);

        List<Long> timestamps = data.getClickTimestamps();
        long burstStart = now - BURST_WINDOW_MS;
        int burstCount = 0;
        for (int i = timestamps.size() - 1; i >= 0; i--) {
            if (timestamps.get(i) >= burstStart) burstCount++;
            else break;
        }

        if (burstCount >= BURST_THRESHOLD) {
            handleViolation(data, player, 6.0, 10.0, 1.0);
        }
    }
}
