package com.yourdomain.guardianac.checks.combat.killaura;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.PingUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;

/**
 * KillAura (F) — Hit accuracy analysis.
 * Tracks the ratio of successful hits to total arm swings.
 * Legitimate players miss; aura clients have near-perfect hit rates.
 */
public class KillAuraF extends Check {

    private static final long WINDOW_MS = 5000;
    private static final int MIN_SWINGS = 10;
    private static final double MAX_HIT_RATIO = 0.92;

    public KillAuraF(GuardianAC plugin) {
        super(plugin, "KillAura", "F", CheckType.COMBAT);
    }

    @EventHandler
    public void onSwing(PlayerAnimationEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long windowMs = PingUtil.adjustTimeWindow(player, WINDOW_MS);

        if (now - data.getSwingWindowStart() > windowMs) {
            // Evaluate previous window
            int swings = data.getSwingCount();
            int hits = data.getHitCount();

            if (swings >= MIN_SWINGS && swings > 0) {
                double ratio = (double) hits / swings;
                if (ratio > MAX_HIT_RATIO) {
                    handleViolation(data, player, 2.0, 7.0, 0.5);
                }
            }

            data.resetSwingCount();
            data.resetHitCount();
            data.setSwingWindowStart(now);
        }

        data.incrementSwingCount();
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        data.incrementHitCount();
    }
}
