package com.yourdomain.guardianac.checks.movement.phasewalk;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * PhaseWalk (B) — Vertical block clip.
 * Flags players moving vertically through solid blocks.
 */
public class PhaseWalkB extends Check {

    public PhaseWalkB(GuardianAC plugin) {
        super(plugin, "PhaseWalk", "B", CheckType.MOVEMENT);
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

        double deltaY = event.getTo().getY() - event.getFrom().getY();
        Location head = event.getTo().clone().add(0, 1.8, 0);

        if (deltaY > 0.1 && head.getBlock().getType().isSolid()) {
            handleViolation(data, player, 5.0, 8.0, 2.0);
        }
    }
}
