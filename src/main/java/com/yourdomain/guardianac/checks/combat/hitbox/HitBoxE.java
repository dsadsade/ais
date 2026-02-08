package com.yourdomain.guardianac.checks.combat.hitbox;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * HitBox (E) — Hitbox during sprint (extended forward hitbox).
 * Detects hits at wide angles while sprinting, indicating hitbox expansion.
 * Sprinting narrows the effective forward hitbox angle.
 */
public class HitBoxE extends Check {

    private static final double MAX_ANGLE_DURING_SPRINT = 70.0;

    public HitBoxE(GuardianAC plugin) {
        super(plugin, "HitBox", "E", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (!player.isSprinting()) return;

        Vector lookDir = player.getEyeLocation().getDirection().normalize();
        Vector toTarget = target.getLocation().toVector()
                .subtract(player.getEyeLocation().toVector()).normalize();

        double angle = Math.toDegrees(lookDir.angle(toTarget));

        if (angle > MAX_ANGLE_DURING_SPRINT) {
            handleViolation(data, player, 5.0, 10.0, 1.0);
        }
    }
}
