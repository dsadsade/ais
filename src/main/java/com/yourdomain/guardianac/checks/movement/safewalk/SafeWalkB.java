package com.yourdomain.guardianac.checks.movement.safewalk;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * SafeWalk (B) — Bridge walking perfection.
 * Detects impossibly perfect bridge placement movement patterns.
 */
public class SafeWalkB extends Check {

    private int perfectBridgeTicks;

    public SafeWalkB(GuardianAC plugin) {
        super(plugin, "SafeWalk", "B", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle() || player.isFlying()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double horizDist = Math.hypot(event.getTo().getX() - event.getFrom().getX(),
                event.getTo().getZ() - event.getFrom().getZ());
        double deltaY = Math.abs(event.getTo().getY() - event.getFrom().getY());

        if (player.isSneaking() && horizDist > 0.05 && horizDist < 0.13 && deltaY == 0.0) {
            perfectBridgeTicks++;
            if (perfectBridgeTicks > 20) {
                handleViolation(data, player, 2.0, 15.0, 1.0);
            }
        } else {
            perfectBridgeTicks = Math.max(0, perfectBridgeTicks - 2);
        }
    }
}
