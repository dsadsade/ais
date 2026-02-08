package com.yourdomain.guardianac.checks.movement.noslowmotion;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * NoSlowMotion (A) — Full speed in soul sand.
 * Flags normal walking speed while on soul sand without Soul Speed enchantment.
 */
public class NoSlowMotionA extends Check {

    private static final double MAX_SOUL_SPEED = 0.08;

    public NoSlowMotionA(GuardianAC plugin) {
        super(plugin, "NoSlowMotion", "A", CheckType.MOVEMENT);
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

        Material below = player.getLocation().clone().subtract(0, 0.2, 0).getBlock().getType();
        if (below != Material.SOUL_SAND) return;

        double horizDist = Math.hypot(event.getTo().getX() - event.getFrom().getX(),
                event.getTo().getZ() - event.getFrom().getZ());

        if (horizDist > MAX_SOUL_SPEED && player.isOnGround()) {
            handleViolation(data, player, 3.0, 10.0, 1.5);
        }
    }
}
