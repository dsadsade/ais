package com.yourdomain.guardianac.checks.combat.aimbot;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Aimbot (D) — Head rotation sync (camera and head desync).
 * Some aimbots rotate the head packet independently from camera rotation.
 */
public class AimbotD extends Check {

    private static final float MAX_DESYNC = 25.0f;

    public AimbotD(GuardianAC plugin) {
        super(plugin, "Aimbot", "D", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float bodyYaw = player.getLocation().getYaw();
        float headYaw = player.getEyeLocation().getYaw();

        float desync = Math.abs(bodyYaw - headYaw);
        if (desync > 180) desync = 360 - desync;

        if (desync > MAX_DESYNC) {
            handleViolation(data, player, (desync - MAX_DESYNC) * 0.3, 10.0, 1.0);
        }
    }
}
