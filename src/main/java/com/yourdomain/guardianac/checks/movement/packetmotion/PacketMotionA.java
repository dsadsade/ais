package com.yourdomain.guardianac.checks.movement.packetmotion;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * PacketMotion (A) — Invalid motion vectors (NaN/Infinity).
 * Flags movement packets containing NaN or Infinity position values.
 */
public class PacketMotionA extends Check {

    public PacketMotionA(GuardianAC plugin) {
        super(plugin, "PacketMotion", "A", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double x = event.getTo().getX();
        double y = event.getTo().getY();
        double z = event.getTo().getZ();

        if (Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z)
                || Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z)) {
            event.setCancelled(true);
            handleViolation(data, player, 10.0, 5.0, 1.0);
        }
    }
}
