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
 * KillAura (I) — Sprint reset analysis.
 * Detects impossible sprint toggling for critical hits.
 * To land a crit, you must not be sprinting when the hit lands.
 * Some clients rapidly toggle sprint for automatic crits — too fast for humans.
 */
public class KillAuraI extends Check {

    private static final long SPRINT_TOGGLE_WINDOW_MS = 2000;
    private static final int MAX_TOGGLES = 6;

    public KillAuraI(GuardianAC plugin) {
        super(plugin, "KillAura", "I", CheckType.COMBAT);
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
        boolean sprinting = player.isSprinting();
        long windowMs = PingUtil.adjustTimeWindow(player, SPRINT_TOGGLE_WINDOW_MS);

        // Detect sprint toggle
        if (sprinting != data.wasSprinting()) {
            data.incrementSprintToggleCount();
            data.setLastSprintToggleTime(now);
        }

        // Reset window if expired
        if (now - data.getSprintToggleWindowStart() > windowMs) {
            data.resetSprintToggleCount();
            data.setSprintToggleWindowStart(now);
        }

        // Not sprinting on hit tick (attempting crit) with rapid toggles
        if (!sprinting && data.wasSprinting()) {
            long timeSinceToggle = now - data.getLastSprintToggleTime();
            if (timeSinceToggle < PingUtil.adjustTimeWindow(player, 50)) {
                int toggles = data.getSprintToggleCount();
                if (toggles >= MAX_TOGGLES) {
                    handleViolation(data, player, 2.5, 7.0, 0.5);
                }
            }
        }

        data.setWasSprinting(sprinting);
        data.setLastAttackTime(now);
    }
}
