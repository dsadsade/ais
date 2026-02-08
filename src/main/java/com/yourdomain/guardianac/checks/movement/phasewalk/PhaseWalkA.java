package com.yourdomain.guardianac.checks.movement.phasewalk;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * PhaseWalk (A) — Horizontal block clip.
 * Flags players moving horizontally into solid blocks.
 */
public class PhaseWalkA extends Check {

    public PhaseWalkA(GuardianAC plugin) {
        super(plugin, "PhaseWalk", "A", CheckType.MOVEMENT);
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

        double horizDist = Math.hypot(event.getTo().getX() - event.getFrom().getX(),
                event.getTo().getZ() - event.getFrom().getZ());

        if (horizDist > 0.1 && event.getTo().getBlock().getType().isSolid()) {
            handleViolation(data, player, 5.0, 8.0, 2.0);
        }
    }
}
