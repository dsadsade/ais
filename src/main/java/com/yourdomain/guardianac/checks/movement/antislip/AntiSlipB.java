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
 * AntiSlip (B) — Instant stop on slippery surfaces.
 * Detects instantaneous velocity change to zero on slime or ice.
 */
public class AntiSlipB extends Check {

    public AntiSlipB(GuardianAC plugin) {
        super(plugin, "AntiSlip", "B", CheckType.MOVEMENT);
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
        boolean slippery = below == Material.ICE || below == Material.PACKED_ICE
                || below == Material.BLUE_ICE || below == Material.SLIME_BLOCK;

        if (!slippery || !player.isOnGround()) return;

        double speed = Math.hypot(event.getTo().getX() - event.getFrom().getX(),
                event.getTo().getZ() - event.getFrom().getZ());

        if (speed == 0.0 && data.getRecentSpeeds().stream().anyMatch(s -> s > 0.2)) {
            handleViolation(data, player, 3.0, 12.0, 1.5);
        }
    }
}
