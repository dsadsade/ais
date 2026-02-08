package com.yourdomain.guardianac.checks.movement.boatspeed;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * BoatSpeed (B) — Boat phase through blocks.
 * Detects boats moving through solid blocks.
 */
public class BoatSpeedB extends Check {

    public BoatSpeedB(GuardianAC plugin) {
        super(plugin, "BoatSpeed", "B", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (isExempt(player)) return;

        if (!player.isInsideVehicle() || !(player.getVehicle() instanceof Boat)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (event.getTo().getBlock().getType().isSolid()) {
            handleViolation(data, player, 5.0, 8.0, 2.0);
        }
    }
}
