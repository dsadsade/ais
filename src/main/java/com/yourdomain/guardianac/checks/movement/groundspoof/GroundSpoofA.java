package com.yourdomain.guardianac.checks.movement.groundspoof;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * GroundSpoof (A) — Claiming on-ground while in air.
 * Checks if the player reports on-ground but there is no solid block beneath them.
 */
public class GroundSpoofA extends Check {

    public GroundSpoofA(GuardianAC plugin) {
        super(plugin, "GroundSpoof", "A", CheckType.MOVEMENT);
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

        if (player.isOnGround()) {
            Location below = player.getLocation().clone().subtract(0, 0.1, 0);
            Block block = below.getBlock();
            if (block.getType() == Material.AIR && !player.isSwimming()) {
                handleViolation(data, player, 4.0, 10.0, 1.5);
            }
        }
    }
}
