package com.yourdomain.guardianac.checks.movement.waterwalk;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * WaterWalk (B) — Hovering at water depth level.
 * Flags staying at a fixed depth while inside water without swimming motion.
 */
public class WaterWalkB extends Check {

    private int hoverInWaterTicks;

    public WaterWalkB(GuardianAC plugin) {
        super(plugin, "WaterWalk", "B", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isInsideVehicle()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaY = Math.abs(event.getTo().getY() - event.getFrom().getY());

        if (player.isInWater() && deltaY < 0.005 && !player.isOnGround()) {
            hoverInWaterTicks++;
            if (hoverInWaterTicks > 15) {
                handleViolation(data, player, 3.0, 12.0, 1.5);
            }
        } else {
            hoverInWaterTicks = Math.max(0, hoverInWaterTicks - 1);
        }
    }
}
