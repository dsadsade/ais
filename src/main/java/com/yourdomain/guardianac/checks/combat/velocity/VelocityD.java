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
 * Velocity (D) — Delayed KB response detection.
 * Detects players who take knockback later than expected, indicating
 * velocity mods that delay KB application.
 */
public class VelocityD extends Check {

    private static final long MAX_KB_DELAY_MS = 200;

    public VelocityD(GuardianAC plugin) {
        super(plugin, "Velocity", "D", CheckType.COMBAT);
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

        long timeSinceDamage = System.currentTimeMillis() - data.getLastDamageTime();
        double deltaH = Math.sqrt(
                Math.pow(event.getTo().getX() - event.getFrom().getX(), 2) +
                Math.pow(event.getTo().getZ() - event.getFrom().getZ(), 2));
        double deltaY = Math.abs(event.getTo().getY() - event.getFrom().getY());

        if (timeSinceDamage > MAX_KB_DELAY_MS && deltaH < 0.05 && deltaY < 0.05) {
            handleViolation(data, player, 4.0, 10.0, 1.0);
        }

        if (timeSinceDamage > 500) {
            data.setPendingVelocityCheck(false);
        }
    }
}
