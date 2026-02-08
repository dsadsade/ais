package com.yourdomain.guardianac.checks.player.timing;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class TimingB extends Check {

    public TimingB(GuardianAC plugin) {
        super(plugin, "Timing", "B", CheckType.PLAYER);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long lastPacket = data.getLastPacketTime();

        if (lastPacket > 0) {
            long delta = now - lastPacket;
            // Packet timing exploitation — perfectly consistent intervals
            data.addTimingDelta(delta);
            double variance = data.getTimingVariance();
            if (variance < 0.5 && data.getTimingSampleCount() > 20) {
                handleViolation(data, player, 5, 10, 0.99);
            }
        }

        data.setLastPacketTime(now);
    }
}
