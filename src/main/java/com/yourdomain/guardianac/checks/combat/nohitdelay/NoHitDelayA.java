package com.yourdomain.guardianac.checks.combat.nohitdelay;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * NoHitDelay (A) — Attack cooldown bypass.
 * Detects attacks that bypass the 1.9+ attack cooldown by checking
 * the time between consecutive damage events against the minimum tick delay.
 */
public class NoHitDelayA extends Check {

    private static final long MIN_HIT_DELAY_MS = 400;
    private static final int CONSECUTIVE_THRESHOLD = 3;

    public NoHitDelayA(GuardianAC plugin) {
        super(plugin, "NoHitDelay", "A", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long timeSinceLastAttack = now - data.getLastAttackTime();

        if (timeSinceLastAttack > 0 && timeSinceLastAttack < MIN_HIT_DELAY_MS) {
            int consecutive = data.getConsecutiveImmediateHits() + 1;
            data.setConsecutiveImmediateHits(consecutive);

            if (consecutive >= CONSECUTIVE_THRESHOLD) {
                handleViolation(data, player, 5.0, 10.0, 1.0);
            }
        } else {
            data.setConsecutiveImmediateHits(0);
        }
    }
}
