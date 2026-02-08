package com.yourdomain.guardianac.checks.player.protocol;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRegisterChannelEvent;

public class ProtocolE extends Check {

    public ProtocolE(GuardianAC plugin) {
        super(plugin, "Protocol", "E", CheckType.PLAYER);
    }

    @EventHandler
    public void onChannelRegister(PlayerRegisterChannelEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        String channel = event.getChannel();

        // Channel registration abuse — excessive or suspicious channels
        if (player.getListeningPluginChannels().size() > 20) {
            handleViolation(data, player, 6, 5, 0.99);
        }

        if (channel.length() > 128) {
            handleViolation(data, player, 8, 3, 0.99);
        }
    }
}
