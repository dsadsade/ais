package com.yourdomain.guardianac.checks.movement.antisneak;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * AntiSneak (A) — Sneak state desync.
 * Flags the player sending sneak state but moving at full walk speed.
 */
public class AntiSneakA extends Check {

    private static final double WALK_SPEED_THRESHOLD = 0.18;

    public AntiSneakA(GuardianAC plugin) {
        super(plugin, "AntiSneak", "A", CheckType.MOVEMENT);
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

        if (!player.isSneaking()) return;

        double horizDist = Math.hypot(event.getTo().getX() - event.getFrom().getX(),
                event.getTo().getZ() - event.getFrom().getZ());

        if (horizDist > WALK_SPEED_THRESHOLD) {
            handleViolation(data, player, 4.0, 10.0, 2.0);
        }
    }
}
