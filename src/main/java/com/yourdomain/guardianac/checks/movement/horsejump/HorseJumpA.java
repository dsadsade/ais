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
 * HorseJump (A) — Mount jump height exceeding maximum.
 * Flags vertical movement on horses beyond the maximum jump strength.
 */
public class HorseJumpA extends Check {

    private static final double MAX_HORSE_JUMP_HEIGHT = 5.5;

    public HorseJumpA(GuardianAC plugin) {
        super(plugin, "HorseJump", "A", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        if (!player.isInsideVehicle() || !(player.getVehicle() instanceof Horse)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        if (deltaY > MAX_HORSE_JUMP_HEIGHT) {
            handleViolation(data, player, 5.0, 8.0, 2.0);
        }
    }
}
