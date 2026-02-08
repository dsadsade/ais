package com.yourdomain.guardianac.checks.movement.phasewalk;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * PhaseWalk (C) — Diagonal phase through corners.
 * Flags movement through diagonal block corners.
 */
public class PhaseWalkC extends Check {

    public PhaseWalkC(GuardianAC plugin) {
        super(plugin, "PhaseWalk", "C", CheckType.MOVEMENT);
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

        Location from = event.getFrom();
        Location to = event.getTo();
        double dx = Math.abs(to.getX() - from.getX());
        double dz = Math.abs(to.getZ() - from.getZ());

        if (dx > 0.1 && dz > 0.1) {
            // Check intermediate diagonal block
            Location mid = from.clone().add(to.getX() - from.getX(), 0, to.getZ() - from.getZ());
            if (mid.getBlock().getType().isSolid()) {
                handleViolation(data, player, 4.0, 10.0, 2.0);
            }
        }
    }
}
