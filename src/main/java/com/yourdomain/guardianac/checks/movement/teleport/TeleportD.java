package com.yourdomain.guardianac.checks.movement.teleport;

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
 * Teleport (D) — Rubber band teleport.
 * Flags back-and-forth movement suggesting position spoofing.
 */
public class TeleportD extends Check {

    private Location prevFrom;

    public TeleportD(GuardianAC plugin) {
        super(plugin, "Teleport", "D", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR || player.isInsideVehicle()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location to = event.getTo();
        if (prevFrom != null && prevFrom.getWorld() == to.getWorld()) {
            double snapBack = prevFrom.distance(to);
            double moved = event.getFrom().distance(to);
            if (snapBack < 0.5 && moved > 3.0) {
                handleViolation(data, player, 4.0, 10.0, 2.0);
            }
        }

        prevFrom = event.getFrom().clone();
    }
}
