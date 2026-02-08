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
 * AutoClicker (D) — Butterfly click pattern detection.
 * Butterfly clicking produces alternating fast/slow intervals with
 * abnormally high CPS (>20) from two-finger alternation.
 */
public class AutoClickerD extends Check {

    private static final int MIN_SAMPLES = 12;
    private static final double MAX_CPS = 22.0;

    public AutoClickerD(GuardianAC plugin) {
        super(plugin, "AutoClicker", "D", CheckType.COMBAT);
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
        if (timestamps.size() < MIN_SAMPLES) return;

        long windowStart = timestamps.get(timestamps.size() - MIN_SAMPLES);
        double windowSec = (now - windowStart) / 1000.0;
        if (windowSec <= 0) return;

        double cps = MIN_SAMPLES / windowSec;
        if (cps > MAX_CPS) {
            handleViolation(data, player, (cps - MAX_CPS) * 2.0, 10.0, 1.0);
        }
    }
}
