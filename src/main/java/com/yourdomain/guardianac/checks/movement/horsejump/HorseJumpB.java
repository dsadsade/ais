package com.yourdomain.guardianac.checks.movement.horsejump;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * HorseJump (B) — Mount speed exploit.
 * Flags horizontal speed on horses exceeding maximum.
 */
public class HorseJumpB extends Check {

    private static final double MAX_HORSE_SPEED = 0.45;

    public HorseJumpB(GuardianAC plugin) {
        super(plugin, "HorseJump", "B", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        if (!player.isInsideVehicle() || !(player.getVehicle() instanceof Horse)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double horizDist = Math.hypot(event.getTo().getX() - event.getFrom().getX(),
                event.getTo().getZ() - event.getFrom().getZ());

        if (horizDist > MAX_HORSE_SPEED) {
            handleViolation(data, player, 3.0, 12.0, 1.5);
        }
    }
}
