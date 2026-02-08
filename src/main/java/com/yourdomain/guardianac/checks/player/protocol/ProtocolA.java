package com.yourdomain.guardianac.checks.player.protocol;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class ProtocolA extends Check {

    public ProtocolA(GuardianAC plugin) {
        super(plugin, "Protocol", "A", CheckType.PLAYER);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long lastTeleport = data.getLastTeleportTime();

        // Invalid packet order — movement before teleport confirm
        if (lastTeleport > 0 && now - lastTeleport < 50) {
            handleViolation(data, player, 6, 8, 0.99);
        }
    }
}
