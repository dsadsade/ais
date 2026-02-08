package com.yourdomain.guardianac.checks.combat.autoclicker;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.PingUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * AutoClicker (A) — CPS rate check.
 * Counts left clicks per second. Legitimate players cap at ~16-20 CPS
 * with butterfly/jitter clicking. We use a generous threshold with ping compensation.
 */
public class AutoClickerA extends Check {

    private static final int MAX_CPS = 20;
    private static final long WINDOW_MS = 1000;

    public AutoClickerA(GuardianAC plugin) {
        super(plugin, "AutoClicker", "A", CheckType.COMBAT);
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
        long windowMs = PingUtil.adjustTimeWindow(player, WINDOW_MS);

        // Record click for interval tracking
        data.addClickTimestamp(now);

        if (now - data.getClickWindowStart() > windowMs) {
            int cps = data.getClickCount();

            if (cps > MAX_CPS) {
                double weight = Math.min(4.0, 1.0 + (cps - MAX_CPS) * 0.5);
                handleViolation(data, player, weight, 6.0, 0.5);
            }

            data.resetClickCount();
            data.setClickWindowStart(now);
        }

        data.incrementClickCount();
    }
}
