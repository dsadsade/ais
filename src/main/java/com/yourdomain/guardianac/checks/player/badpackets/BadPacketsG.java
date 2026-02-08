package com.yourdomain.guardianac.checks.player.badpackets;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class BadPacketsG extends Check {

    public BadPacketsG(GuardianAC plugin) {
        super(plugin, "BadPackets", "G", CheckType.PLAYER);
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

        // Packet sequence timing anomaly — too consistent or irregular
        if (lastPacket > 0 && (delta < 2 || delta > 2000)) {
            handleViolation(data, player, 3, 12, 0.995);
        }

        data.setLastPacketTime(now);
    }
}
