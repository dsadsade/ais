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
 * ClickPattern (A) — Repetitive interval detection.
 * Detects clicks with an unnaturally repetitive (low deviation) interval
 * pattern, indicating a clicker or macro.
 */
public class ClickPatternA extends Check {

    private static final int MIN_CLICKS = 10;
    private static final double MAX_STD_DEV = 5.0;

    public ClickPatternA(GuardianAC plugin) {
        super(plugin, "ClickPattern", "A", CheckType.COMBAT);
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

        double stdDev = MathUtil.standardDeviation(intervals);

        if (stdDev < MAX_STD_DEV) {
            handleViolation(data, player, 4.0, 12.0, 1.0);
        }
    }
}
