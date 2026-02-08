package com.yourdomain.guardianac.checks.player.badpackets;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class BadPacketsE extends Check {

    public BadPacketsE(GuardianAC plugin) {
        super(plugin, "BadPackets", "E", CheckType.PLAYER);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long lastMove = data.getLastMoveTime();

        if (now - lastMove < 5 && lastMove > 0) {
            // Duplicate movement packets detected within impossibly short window
            handleViolation(data, player, 5, 10, 0.995);
        }

        data.setLastMoveTime(now);
    }
}
