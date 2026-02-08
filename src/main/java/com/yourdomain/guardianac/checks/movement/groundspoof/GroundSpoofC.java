package com.yourdomain.guardianac.checks.movement.groundspoof;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * GroundSpoof (C) — Position desync with ground state.
 * Detects when Y movement contradicts the reported ground state.
 */
public class GroundSpoofC extends Check {

    public GroundSpoofC(GuardianAC plugin) {
        super(plugin, "GroundSpoof", "C", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isInsideVehicle() || player.isGliding()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        // Claiming on ground but moving vertically upward significantly
        if (player.isOnGround() && deltaY > 0.5) {
            handleViolation(data, player, 5.0, 10.0, 2.0);
        }
    }
}
