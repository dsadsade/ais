package com.yourdomain.guardianac.checks.player.protocol;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class ProtocolB extends Check {

    public ProtocolB(GuardianAC plugin) {
        super(plugin, "Protocol", "B", CheckType.PLAYER);
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
        long delta = now - lastPacket;

        // Packet timing anomaly — consistent sub-tick intervals
        if (lastPacket > 0 && delta > 0 && delta < 10) {
            handleViolation(data, player, 3, 15, 0.995);
        }
    }
}
