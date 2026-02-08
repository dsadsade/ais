package com.yourdomain.guardianac.checks.combat.aimbot;

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
 * Aimbot (F) — FOV lock (aim restricted to narrow cone).
 * The player's look direction always stays within a small angle of the
 * target, as if locked to a narrow field of view.
 */
public class AimbotF extends Check {

    private static final double FOV_THRESHOLD = 5.0;
    private static final int LOCK_STREAK_THRESHOLD = 6;

    public AimbotF(GuardianAC plugin) {
        super(plugin, "Aimbot", "F", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Vector lookDir = player.getEyeLocation().getDirection().normalize();
        Vector toTarget = target.getEyeLocation().toVector()
                .subtract(player.getEyeLocation().toVector()).normalize();

        double angle = Math.toDegrees(lookDir.angle(toTarget));

        if (angle < FOV_THRESHOLD) {
            data.setPerfectAimCount(data.getPerfectAimCount() + 1);
        } else {
            data.setPerfectAimCount(0);
        }

        if (data.getPerfectAimCount() >= LOCK_STREAK_THRESHOLD) {
            handleViolation(data, player, 4.0, 12.0, 1.0);
            data.setPerfectAimCount(0);
        }
    }
}
