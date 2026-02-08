package com.yourdomain.guardianac.checks.movement.waterwalk;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * WaterWalk (A) — Walking on water surface.
 * Flags movement on top of water without sinking.
 */
public class WaterWalkA extends Check {

    private int waterWalkTicks;

    public WaterWalkA(GuardianAC plugin) {
        super(plugin, "WaterWalk", "A", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isInsideVehicle() || player.isGliding()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location below = player.getLocation().clone().subtract(0, 0.1, 0);

        if (below.getBlock().getType() == Material.WATER && player.isOnGround()) {
            waterWalkTicks++;
            if (waterWalkTicks > 8) {
                handleViolation(data, player, 4.0, 10.0, 2.0);
            }
        } else {
            waterWalkTicks = Math.max(0, waterWalkTicks - 1);
        }
    }
}
