package com.yourdomain.guardianac.checks.movement.faststair;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * FastStair (B) — Stair skip.
 * Flags going up 2+ stair blocks in a single tick.
 */
public class FastStairB extends Check {

    public FastStairB(GuardianAC plugin) {
        super(plugin, "FastStair", "B", CheckType.MOVEMENT);
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

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        if (player.isOnGround() && deltaY > 1.0 && deltaY < 2.5) {
            handleViolation(data, player, 4.0, 10.0, 2.0);
        }
    }
}
