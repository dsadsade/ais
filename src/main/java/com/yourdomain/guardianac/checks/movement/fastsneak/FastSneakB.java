package com.yourdomain.guardianac.checks.movement.fastsneak;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * FastSneak (B) — Sneak animation bypass.
 * Detects full-speed movement while the client claims to be sneaking.
 */
public class FastSneakB extends Check {

    private static final double WALK_SPEED = 0.21;

    public FastSneakB(GuardianAC plugin) {
        super(plugin, "FastSneak", "B", CheckType.MOVEMENT);
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

        if (horizDist >= WALK_SPEED) {
            handleViolation(data, player, 5.0, 8.0, 2.0);
        }
    }
}
