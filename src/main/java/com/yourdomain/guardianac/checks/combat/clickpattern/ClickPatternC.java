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
 * ClickPattern (C) — Statistical distribution analysis.
 * Uses kurtosis and skewness to detect non-human click distributions.
 * Human clicks typically follow a mesokurtic/normal distribution.
 */
public class ClickPatternC extends Check {

    private static final int MIN_CLICKS = 15;

    public ClickPatternC(GuardianAC plugin) {
        super(plugin, "ClickPattern", "C", CheckType.COMBAT);
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

        List<Long> intervals = data.getExtendedClickIntervals();
        if (intervals.size() < MIN_CLICKS) return;

        double kurtosis = MathUtil.kurtosis(intervals);
        double skewness = MathUtil.skewness(intervals);

        boolean leptokurtic = kurtosis > 10.0;
        boolean tooSymmetric = Math.abs(skewness) < 0.1;

        if (leptokurtic && tooSymmetric) {
            handleViolation(data, player, 4.0, 12.0, 1.0);
        }
    }
}
