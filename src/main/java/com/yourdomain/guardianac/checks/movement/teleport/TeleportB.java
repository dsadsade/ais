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
 * Teleport (B) — Blink teleport.
 * Detects no movement packets followed by a large position jump.
 */
public class TeleportB extends Check {

    private static final double BLINK_DIST_THRESHOLD = 4.0;
    private static final long BLINK_GAP_MS = 1000;

    public TeleportB(GuardianAC plugin) {
        super(plugin, "Teleport", "B", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long gap = now - data.getLastMovePacketTime();
        double dist = event.getFrom().distance(event.getTo());

        if (gap > BLINK_GAP_MS && dist > BLINK_DIST_THRESHOLD) {
            handleViolation(data, player, 5.0, 10.0, 2.0);
        }

        data.setLastMovePacketTime(now);
    }
}
