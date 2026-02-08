package com.yourdomain.guardianac.checks.combat.autoblock;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * AutoBlock (B) — Attack-block pattern detection.
 * Detects alternating attack/block patterns that are frame-perfect,
 * indicating a macro or automated tool.
 */
public class AutoBlockB extends Check {

    private static final long PATTERN_WINDOW_MS = 100;

    public AutoBlockB(GuardianAC plugin) {
        super(plugin, "AutoBlock", "B", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long lastBlock = data.getLastBlockTime();
        long lastAttack = data.getLastAttackTime();

        if (lastBlock > 0 && lastAttack > 0) {
            long blockAttackGap = Math.abs(now - lastBlock);
            long prevAttackBlockGap = Math.abs(lastBlock - lastAttack);

            if (blockAttackGap < PATTERN_WINDOW_MS && prevAttackBlockGap < PATTERN_WINDOW_MS) {
                handleViolation(data, player, 4.0, 10.0, 1.0);
            }
        }
    }
}
