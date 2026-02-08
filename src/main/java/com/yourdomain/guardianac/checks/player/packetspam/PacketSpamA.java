package com.yourdomain.guardianac.checks.player.packetspam;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class PacketSpamA extends Check {

    public PacketSpamA(GuardianAC plugin) {
        super(plugin, "PacketSpam", "A", CheckType.PLAYER);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        int moveCount = data.incrementMovePacketCount();

        // Movement packet flooding — exceeds expected tick rate
        if (moveCount > 25) {
            handleViolation(data, player, 6, 10, 0.99);
        }
    }
}
