package com.yourdomain.guardianac.checks.combat.autoblock;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * AutoBlock (A) — Shield toggle timing analysis.
 * Detects players toggling shield use and attack within an impossibly
 * short time window, indicating automated blocking.
 */
public class AutoBlockA extends Check {

    private static final long MIN_BLOCK_DELAY_MS = 50;

    public AutoBlockA(GuardianAC plugin) {
        super(plugin, "AutoBlock", "A", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long timeSinceBlock = now - data.getLastBlockTime();

        if (data.wasBlocking() && timeSinceBlock < MIN_BLOCK_DELAY_MS) {
            int count = data.getShieldToggleCount();
            if (count > 4) {
                handleViolation(data, player, 5.0, 10.0, 1.0);
            }
        }
    }
}
