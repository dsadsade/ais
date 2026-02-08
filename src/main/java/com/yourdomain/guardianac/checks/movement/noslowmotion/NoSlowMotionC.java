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
 * NoSlowMotion (C) — Full speed in cobwebs (enhanced).
 * Flags normal movement speed while inside cobweb blocks.
 */
public class NoSlowMotionC extends Check {

    private static final double MAX_WEB_SPEED = 0.03;

    public NoSlowMotionC(GuardianAC plugin) {
        super(plugin, "NoSlowMotion", "C", CheckType.MOVEMENT);
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

        Material inBlock = player.getLocation().getBlock().getType();
        if (inBlock != Material.COBWEB) return;

        double horizDist = Math.hypot(event.getTo().getX() - event.getFrom().getX(),
                event.getTo().getZ() - event.getFrom().getZ());

        if (horizDist > MAX_WEB_SPEED) {
            handleViolation(data, player, 4.0, 8.0, 2.0);
        }
    }
}
