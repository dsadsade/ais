package com.yourdomain.guardianac.checks.movement.antislip;

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
 * AntiSlip (A) — Normal friction on ice blocks.
 * Flags players with normal movement friction while on ice.
 */
public class AntiSlipA extends Check {

    public AntiSlipA(GuardianAC plugin) {
        super(plugin, "AntiSlip", "A", CheckType.MOVEMENT);
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

        Material below = player.getLocation().clone().subtract(0, 1, 0).getBlock().getType();
        boolean onIce = below == Material.ICE || below == Material.PACKED_ICE || below == Material.BLUE_ICE;

        if (!onIce || !player.isOnGround()) return;

        double horizDist = Math.hypot(event.getTo().getX() - event.getFrom().getX(),
                event.getTo().getZ() - event.getFrom().getZ());
        double horizPrev = Math.hypot(event.getFrom().getX() - data.getLastDeltaX(),
                event.getFrom().getZ() - data.getLastDeltaZ());

        // On ice, sudden stop is suspicious (ice has low friction)
        if (horizPrev > 0.15 && horizDist < 0.02) {
            handleViolation(data, player, 3.0, 12.0, 1.5);
        }
    }
}
