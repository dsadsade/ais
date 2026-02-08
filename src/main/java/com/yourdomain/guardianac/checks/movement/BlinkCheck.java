package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.PingUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Detects Blink/Fake-lag — players holding movement packets and releasing them in bursts.
 * This results in long gaps between movements followed by sudden teleportation.
 * Normal gameplay has consistent ~50ms gaps between movement packets.
 */
public class BlinkCheck extends Check {

    private static final long MAX_MOVE_GAP_MS = 2000; // 2 seconds without movement is suspicious
    private static final int CONSECUTIVE_GAPS = 3;

    public BlinkCheck(GuardianAC plugin) {
        super(plugin, "Blink", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.isInsideVehicle() || player.isFlying()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;
        if (isExempt(player)) return;

        long now = System.currentTimeMillis();
        long lastMove = data.getLastRealMoveTime();

        if (lastMove > 0) {
            long gap = now - lastMove;
            data.addMovementGap(gap);

            // Adjust threshold for high-ping players
            long maxGap = PingUtil.adjustTimeWindow(player, MAX_MOVE_GAP_MS);

            if (gap > maxGap) {
                data.setBlinkTicks(data.getBlinkTicks() + 1);

                // Check if the player teleported a large distance after the gap
                double dist = event.getFrom().distance(event.getTo());
                if (dist > 5.0 && data.getBlinkTicks() >= 2) {
                    handleViolation(data, player, 6.0, 12.0, 1.5);
                    data.setBlinkTicks(0);
                }
            } else {
                data.setBlinkTicks(Math.max(0, data.getBlinkTicks() - 1));
            }
        }

        data.setLastRealMoveTime(now);
    }
}
