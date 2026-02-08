package com.yourdomain.guardianac.checks.combat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Detects abnormally high hit success rate — ratio of successful hits
 * to total arm swings. Legit players miss frequently; aura users hit nearly every swing.
 */
public class HitRateCheck extends Check {

    private static final double MAX_HIT_RATE = 0.92;
    private static final int MIN_SAMPLE_SIZE = 20;

    private final Map<UUID, int[]> swingHitData = new ConcurrentHashMap<>(); // [swings, hits]

    public HitRateCheck(GuardianAC plugin) {
        super(plugin, "HitRate", CheckType.COMBAT);
    }

    @EventHandler
    public void onSwing(PlayerAnimationEvent event) {
        if (!isEnabled()) return;
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        swingHitData.computeIfAbsent(player.getUniqueId(), k -> new int[]{0, 0})[0]++;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (isExempt(player)) return;

        int[] data = swingHitData.computeIfAbsent(player.getUniqueId(), k -> new int[]{0, 0});
        data[1]++;

        if (data[0] >= MIN_SAMPLE_SIZE) {
            double rate = (double) data[1] / data[0];
            if (rate > MAX_HIT_RATE) {
                flag(player, String.format("rate=%.2f swings=%d hits=%d", rate, data[0], data[1]));
            }
            // Reset for next sample
            data[0] = 0;
            data[1] = 0;
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        swingHitData.remove(event.getPlayer().getUniqueId());
    }
}
