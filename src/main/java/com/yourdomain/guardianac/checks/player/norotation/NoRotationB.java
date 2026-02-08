package com.yourdomain.guardianac.checks.player.norotation;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class NoRotationB extends Check {

    public NoRotationB(GuardianAC plugin) {
        super(plugin, "NoRotation", "B", CheckType.PLAYER);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double distSq = event.getFrom().distanceSquared(event.getTo());
        if (distSq < 0.01) return;

        float yaw = player.getLocation().getYaw();
        float lastYaw = data.getLastYaw();

        // Body-head rotation mismatch while moving — yaw unchanged during movement
        if (Math.abs(yaw - lastYaw) < 0.01 && distSq > 0.1) {
            int streak = data.incrementRotationMismatchStreak();
            if (streak > 20) {
                handleViolation(data, player, 4, 10, 0.995);
            }
        } else {
            data.resetRotationMismatchStreak();
        }
    }
}
