package com.yourdomain.guardianac.checks.combat.velocity;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Velocity (G) — KB during sprint detection.
 * In vanilla, receiving knockback cancels sprinting for a few ticks.
 * Velocity mods allow maintaining sprint through knockback.
 */
public class VelocityG extends Check {

    public VelocityG(GuardianAC plugin) {
        super(plugin, "Velocity", "G", CheckType.COMBAT);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        data.setLastDamageTime(System.currentTimeMillis());
        data.setPendingVelocityCheck(true);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null || !data.isPendingVelocityCheck()) return;

        long elapsed = System.currentTimeMillis() - data.getLastDamageTime();
        if (elapsed > 50 && elapsed < 300) {
            if (player.isSprinting()) {
                handleViolation(data, player, 4.0, 12.0, 1.0);
            }
            if (elapsed > 200) {
                data.setPendingVelocityCheck(false);
            }
        }
    }
}
