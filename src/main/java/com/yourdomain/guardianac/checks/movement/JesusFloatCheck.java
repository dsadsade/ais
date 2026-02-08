package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Detects JesusFloat — standing or walking on water/lava surface
 * without being in a boat or using frost walker enchantment.
 */
public class JesusFloatCheck extends Check {

    public JesusFloatCheck(GuardianAC plugin) {
        super(plugin, "JesusFloat", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isInsideVehicle()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Block below = player.getLocation().subtract(0, 0.1, 0).getBlock();
        Block at = player.getLocation().getBlock();

        boolean onLiquidSurface = (below.getType() == Material.WATER || below.getType() == Material.LAVA)
                && at.getType() == Material.AIR;

        if (onLiquidSurface) {
            double dy = event.getTo().getY() - event.getFrom().getY();
            // Player is floating on liquid with no vertical change (not sinking)
            if (Math.abs(dy) < 0.01 && !player.isSwimming()) {
                flag(player, String.format("floating on %s dy=%.4f", below.getType().name(), dy));
            }
        }
    }
}
