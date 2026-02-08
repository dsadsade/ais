package com.yourdomain.guardianac.checks.combat.clickpattern;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

/**
 * ClickPattern (B) — Macro pattern detection (repeating sequence).
 * Detects GCD (Greatest Common Divisor) in click intervals which
 * indicates a macro replaying the same sequence.
 */
public class ClickPatternB extends Check {

    private static final int MIN_CLICKS = 8;

    public ClickPatternB(GuardianAC plugin) {
        super(plugin, "ClickPattern", "B", CheckType.COMBAT);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;
        if (event.getAction() != Action.LEFT_CLICK_AIR
                && event.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        List<Long> intervals = data.getClickIntervals();
        if (intervals.size() < MIN_CLICKS) return;

        double gcd = MathUtil.gcd(intervals);

        if (gcd > 20 && gcd < 200) {
            double maxInterval = intervals.stream().mapToLong(Long::longValue).max().orElse(0);
            double minInterval = intervals.stream().mapToLong(Long::longValue).min().orElse(0);

            if (maxInterval - minInterval < gcd * 0.5) {
                handleViolation(data, player, 4.0, 12.0, 1.0);
            }
        }
    }
}
