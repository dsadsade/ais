package com.yourdomain.guardianac.checks.combat.clickpattern;

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
 * ClickPattern (D) — Click timing entropy analysis.
 * Low entropy in click intervals indicates machine-generated clicks.
 * Human clicks have high entropy due to natural variation.
 */
public class ClickPatternD extends Check {

    private static final int MIN_CLICKS = 12;
    private static final double MIN_ENTROPY = 1.5;

    public ClickPatternD(GuardianAC plugin) {
        super(plugin, "ClickPattern", "D", CheckType.COMBAT);
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

        double entropy = calculateEntropy(intervals);

        if (entropy < MIN_ENTROPY) {
            handleViolation(data, player, 4.0, 12.0, 1.0);
        }
    }

    private double calculateEntropy(List<Long> values) {
        java.util.Map<Long, Integer> freq = new java.util.HashMap<>();
        for (long v : values) {
            long bucket = v / 10 * 10;
            freq.merge(bucket, 1, Integer::sum);
        }

        double entropy = 0;
        int total = values.size();
        for (int count : freq.values()) {
            double p = (double) count / total;
            if (p > 0) entropy -= p * Math.log(p) / Math.log(2);
        }
        return entropy;
    }
}
