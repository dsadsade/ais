package com.yourdomain.guardianac.checks.combat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

/**
 * Detects SilentAim — hitting entities without actually looking at them.
 * The player's server-side rotation doesn't point toward the target
 * but damage packets are still sent (packet-level aim manipulation).
 */
public class SilentAimCheck extends Check {

    private static final double MAX_ANGLE_OFFSET = 60.0; // degrees

    public SilentAimCheck(GuardianAC plugin) {
        super(plugin, "SilentAim", CheckType.COMBAT);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location playerLoc = player.getEyeLocation();
        Location targetLoc = event.getEntity().getLocation().add(0, event.getEntity().getHeight() / 2, 0);

        Vector toTarget = targetLoc.toVector().subtract(playerLoc.toVector()).normalize();
        Vector lookDir = playerLoc.getDirection().normalize();

        double angle = Math.toDegrees(lookDir.angle(toTarget));

        if (angle > MAX_ANGLE_OFFSET) {
            flag(player, String.format("angle=%.1f° max=%.1f°", angle, MAX_ANGLE_OFFSET));
        }
    }
}
