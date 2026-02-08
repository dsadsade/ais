package com.yourdomain.guardianac.checks.combat.autoblock;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * AutoBlock (C) — Sprint-block combination.
 * Detects players who block and sprint simultaneously, which requires
 * specific packet manipulation in newer versions.
 */
public class AutoBlockC extends Check {

    public AutoBlockC(GuardianAC plugin) {
        super(plugin, "AutoBlock", "C", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        boolean sprinting = player.isSprinting() || data.wasSprinting();
        boolean blocking = player.isBlocking() || data.wasBlocking();

        if (sprinting && blocking) {
            long now = System.currentTimeMillis();
            long timeSinceBlock = now - data.getLastBlockTime();

            if (timeSinceBlock < 150) {
                handleViolation(data, player, 5.0, 10.0, 1.0);
            }
        }
    }
}
