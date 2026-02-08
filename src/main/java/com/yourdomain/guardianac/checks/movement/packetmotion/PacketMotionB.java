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
 * PacketMotion (B) — Velocity exceeding max possible.
 * Flags movement speed physically impossible in vanilla.
 */
public class PacketMotionB extends Check {

    private static final double MAX_POSSIBLE_SPEED = 3.92;

    public PacketMotionB(GuardianAC plugin) {
        super(plugin, "PacketMotion", "B", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double dist = event.getFrom().distance(event.getTo());

        if (dist > MAX_POSSIBLE_SPEED && !player.isRiptiding()) {
            event.setCancelled(true);
            handleViolation(data, player, 8.0, 8.0, 2.0);
        }
    }
}
