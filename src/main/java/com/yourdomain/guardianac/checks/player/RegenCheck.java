package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;

/**
 * Detects Regen (FastHeal) hacks — regenerating health faster than vanilla allows.
 * Vanilla natural regen: ~1 HP every 4 seconds when hunger >= 18 (saturation heal) or
 * ~1 HP every 4 seconds from natural regen. With saturation: faster but capped.
 * Regen hacks bypass these delays to heal instantly or at an accelerated rate.
 */
public class RegenCheck extends Check {

    private static final long MIN_REGEN_INTERVAL_MS = 400; // minimum time between natural regen events
    private static final double MAX_HEAL_PER_EVENT = 2.0; // max HP per regen event (1 heart)

    public RegenCheck(GuardianAC plugin) {
        super(plugin, "Regen", CheckType.PLAYER);
    }

    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (isExempt(player)) return;

        // Only check natural regen and saturation regen
        EntityRegainHealthEvent.RegainReason reason = event.getRegainReason();
        if (reason != EntityRegainHealthEvent.RegainReason.SATIATED
                && reason != EntityRegainHealthEvent.RegainReason.REGEN) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long lastCheck = data.getLastHealthCheckTime();

        if (lastCheck > 0) {
            long interval = now - lastCheck;

            // Regen happening too fast
            if (interval < MIN_REGEN_INTERVAL_MS) {
                handleViolation(data, player, 4.0, 10.0, 1.0);
            }
        }

        // Check for impossibly large heal amounts
        if (event.getAmount() > MAX_HEAL_PER_EVENT) {
            handleViolation(data, player, event.getAmount() * 2.0, 8.0, 0.5);
        }

        data.setLastHealthCheckTime(now);
        data.addHealthHistory(player.getHealth() + event.getAmount());
    }
}
