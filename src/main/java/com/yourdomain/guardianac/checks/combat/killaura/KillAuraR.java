package com.yourdomain.guardianac.checks.combat.killaura;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * KillAura (R) — Y-axis head snap on attack.
 * Detects sudden pitch snaps to target on attack ticks. Aimbots snap
 * pitch to exact target Y instantly after no prior pitch movement.
 */
public class KillAuraR extends Check {

    private static final float SNAP_THRESHOLD = 15.0f;

    public KillAuraR(GuardianAC plugin) {
        super(plugin, "KillAura", "R", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float currentPitch = player.getLocation().getPitch();
        float deltaPitch = Math.abs(currentPitch - data.getLastPitch());
        float prevDeltaPitch = Math.abs(data.getLastPitch() - data.getPrevLastPitch());

        if (deltaPitch > SNAP_THRESHOLD && prevDeltaPitch < 1.0f) {
            double targetY = target.getLocation().getY() + target.getHeight() / 2.0;
            double playerEyeY = player.getEyeLocation().getY();
            double yDiff = targetY - playerEyeY;
            boolean snappedToward = (yDiff < 0 && currentPitch > 0) || (yDiff > 0 && currentPitch < 0);

            if (snappedToward || deltaPitch > 30.0f) {
                handleViolation(data, player, 5.0, 12.0, 1.0);
            }
        }
    }
}
