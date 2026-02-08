package com.yourdomain.guardianac.checks.combat.killaura;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * KillAura (S) — Attack packet ordering analysis.
 * Detects when attack packets arrive before corresponding look packets.
 * In vanilla, rotation updates are sent before attack interactions.
 */
public class KillAuraS extends Check {

    public KillAuraS(GuardianAC plugin) {
        super(plugin, "KillAura", "S", CheckType.COMBAT);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float preYaw = player.getLocation().getYaw();
        float prePitch = player.getLocation().getPitch();
        data.setPreAttackRotation(preYaw, prePitch);

        getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
            if (!player.isOnline()) return;
            float postYaw = player.getLocation().getYaw();
            float postPitch = player.getLocation().getPitch();

            float yawDiff = Math.abs(postYaw - preYaw);
            if (yawDiff > 180) yawDiff = 360 - yawDiff;
            float pitchDiff = Math.abs(postPitch - prePitch);

            if (yawDiff > 10.0f && pitchDiff > 5.0f) {
                handleViolation(data, player, 5.0, 15.0, 1.0);
            }
        }, 1L);
    }
}
