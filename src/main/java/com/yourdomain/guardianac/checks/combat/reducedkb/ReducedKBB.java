package com.yourdomain.guardianac.checks.combat.reducedkb;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * ReducedKB (B) — Vertical knockback dampening.
 * Detects players who take reduced vertical knockback while
 * receiving the full horizontal component.
 */
public class ReducedKBB extends Check {

    private static final double VERTICAL_RATIO_THRESHOLD = 0.3;

    public ReducedKBB(GuardianAC plugin) {
        super(plugin, "ReducedKB", "B", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (!data.isPendingVelocityCheck()) return;

        double expectedY = data.getLastVelocityY();
        if (Math.abs(expectedY) < 0.1) return;

        double actualY = player.getVelocity().getY();
        double verticalRatio = actualY / expectedY;

        if (verticalRatio < VERTICAL_RATIO_THRESHOLD && verticalRatio >= 0) {
            handleViolation(data, player, 5.0, 10.0, 1.0);
        }
    }
}
