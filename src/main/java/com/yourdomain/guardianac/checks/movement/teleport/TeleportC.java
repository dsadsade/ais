package com.yourdomain.guardianac.checks.movement.teleport;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Teleport (C) — Sequential micro-teleport.
 * Flags many small but impossible position jumps in rapid succession.
 */
public class TeleportC extends Check {

    private int microTeleportCount;

    public TeleportC(GuardianAC plugin) {
        super(plugin, "Teleport", "C", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR || player.isInsideVehicle()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double dist = event.getFrom().distance(event.getTo());

        if (dist > 1.5 && dist < 5.0) {
            microTeleportCount++;
            if (microTeleportCount > 5) {
                handleViolation(data, player, 4.0, 10.0, 2.0);
                microTeleportCount = 0;
            }
        } else {
            microTeleportCount = Math.max(0, microTeleportCount - 1);
        }
    }
}
