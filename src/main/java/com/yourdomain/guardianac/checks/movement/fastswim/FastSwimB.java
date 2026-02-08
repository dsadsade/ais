package com.yourdomain.guardianac.checks.movement.fastswim;

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
 * FastSwim (B) — Underwater movement speed.
 * Flags excessive speed while fully submerged in water.
 */
public class FastSwimB extends Check {

    private static final double MAX_UNDERWATER_SPEED = 0.12;

    public FastSwimB(GuardianAC plugin) {
        super(plugin, "FastSwim", "B", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle() || player.isFlying()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (!player.isInWater()) return;
        if (player.getEyeLocation().getBlock().getType() != Material.WATER) return;

        double horizDist = Math.hypot(event.getTo().getX() - event.getFrom().getX(),
                event.getTo().getZ() - event.getFrom().getZ());

        if (horizDist > MAX_UNDERWATER_SPEED) {
            handleViolation(data, player, 3.0, 12.0, 1.5);
        }
    }
}
