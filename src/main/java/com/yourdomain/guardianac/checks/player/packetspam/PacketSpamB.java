package com.yourdomain.guardianac.checks.player.packetspam;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PacketSpamB extends Check {

    public PacketSpamB(GuardianAC plugin) {
        super(plugin, "PacketSpam", "B", CheckType.PLAYER);
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long lastAction = data.getLastEntityActionTime();

        // Action packet flooding — rapid entity action toggle
        if (lastAction > 0 && now - lastAction < 15) {
            handleViolation(data, player, 5, 10, 0.995);
        }

        data.setLastEntityActionTime(now);
    }
}
