package com.yourdomain.guardianac.checks.player.norotation;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class NoRotationA extends Check {

    public NoRotationA(GuardianAC plugin) {
        super(plugin, "NoRotation", "A", CheckType.PLAYER);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();

        // Head rotation freeze — 0 rotation for many ticks while moving
        if (yaw == data.getLastYaw() && pitch == data.getLastPitch()) {
            int frozenTicks = data.incrementRotationFrozenTicks();
            if (frozenTicks > 40 && event.getFrom().distanceSquared(event.getTo()) > 0.01) {
                handleViolation(data, player, 5, 8, 0.99);
            }
        } else {
            data.resetRotationFrozenTicks();
        }

        data.setLastYaw(yaw);
        data.setLastPitch(pitch);
    }
}
