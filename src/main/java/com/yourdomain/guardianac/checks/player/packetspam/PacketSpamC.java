package com.yourdomain.guardianac.checks.player.packetspam;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

public class PacketSpamC extends Check {

    public PacketSpamC(GuardianAC plugin) {
        super(plugin, "PacketSpam", "C", CheckType.PLAYER);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long lastInteract = data.getLastInteractTime();

        // Block interaction packet flooding
        if (lastInteract > 0 && now - lastInteract < 10) {
            handleViolation(data, player, 4, 12, 0.995);
        }

        data.setLastInteractTime(now);
    }
}
