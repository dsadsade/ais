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
 * KillAura (N) — Detects attack packets sent while riding a vehicle.
 * In vanilla, players inside boats/minecarts cannot attack entities.
 */
public class KillAuraN extends Check {

    public KillAuraN(GuardianAC plugin) {
        super(plugin, "KillAura", "N", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (player.isInsideVehicle() && player.getVehicle() != null) {
            String vehicleType = player.getVehicle().getType().name();
            if (vehicleType.contains("BOAT") || vehicleType.contains("MINECART")) {
                handleViolation(data, player, 10.0, 10.0, 0.5);
            }
        }
    }
}
