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

/**
 * KillAura (H) — AutoBlock detection.
 * Detects blocking (shield/sword) between hits with impossibly small gaps.
 * Legitimate players can't block and attack on the same tick.
 */
public class KillAuraH extends Check {

    private static final long MIN_BLOCK_GAP_MS = 50;

    public KillAuraH(GuardianAC plugin) {
        super(plugin, "KillAura", "H", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        boolean blocking = player.isBlocking();

        if (blocking) {
            // Player is blocking AND dealing damage on the same tick
            handleViolation(data, player, 3.5, 5.0, 0.25);
        }

        // Check if the gap between unblocking and hitting is impossibly small
        if (data.wasBlocking() && !blocking) {
            long gap = now - data.getLastBlockEndTime();
            long minGap = PingUtil.adjustTimeWindow(player, MIN_BLOCK_GAP_MS);
            if (gap < minGap && gap >= 0) {
                handleViolation(data, player, 2.0, 5.0, 0.25);
            }
        }

        // Update blocking state
        if (blocking && !data.wasBlocking()) {
            data.setLastBlockTime(now);
        } else if (!blocking && data.wasBlocking()) {
            data.setLastBlockEndTime(now);
        }
        data.setWasBlocking(blocking);
        data.setLastAttackTime(now);
    }
}
