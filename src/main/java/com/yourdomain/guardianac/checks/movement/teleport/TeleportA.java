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
 * Teleport (A) — Position jump detection.
 * Flags large instant position changes without server-side teleport.
 */
public class TeleportA extends Check {

    private static final double MAX_MOVE_DIST = 8.0;

    public TeleportA(GuardianAC plugin) {
        super(plugin, "Teleport", "A", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle() || player.isRiptiding()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double dist = event.getFrom().distance(event.getTo());

        if (dist > MAX_MOVE_DIST) {
            handleViolation(data, player, 6.0, 8.0, 2.0);
        }
    }
}
