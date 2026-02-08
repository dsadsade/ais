package com.yourdomain.guardianac.checks.player.protocol;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class ProtocolC extends Check {

    public ProtocolC(GuardianAC plugin) {
        super(plugin, "Protocol", "C", CheckType.PLAYER);
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

        // Duplicate packet detection — identical timestamps
        if (lastMove > 0 && now == lastMove) {
            handleViolation(data, player, 5, 10, 0.99);
        }

        data.setLastMoveTime(now);
    }
}
