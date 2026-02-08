package com.yourdomain.guardianac.checks.movement.faststair;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Tag;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * FastStair (A) — Stair ascent speed.
 * Flags ascending stairs faster than vanilla allows.
 */
public class FastStairA extends Check {

    private static final double MAX_STAIR_SPEED = 0.25;

    public FastStairA(GuardianAC plugin) {
        super(plugin, "FastStair", "A", CheckType.MOVEMENT);
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
        boolean onStairs = Tag.STAIRS.isTagged(player.getLocation().getBlock().getType());

        if (onStairs && deltaY > 0) {
            double horizDist = Math.hypot(event.getTo().getX() - event.getFrom().getX(),
                    event.getTo().getZ() - event.getFrom().getZ());
            if (horizDist > MAX_STAIR_SPEED) {
                handleViolation(data, player, 3.0, 12.0, 1.5);
            }
        }
    }
}
