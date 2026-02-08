package com.yourdomain.guardianac.checks.player.protocol;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class ProtocolD extends Check {

    public ProtocolD(GuardianAC plugin) {
        super(plugin, "Protocol", "D", CheckType.PLAYER);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Modified client brand detection — non-vanilla brand strings
        String brand = data.getClientBrand();
        if (brand != null && !brand.equalsIgnoreCase("vanilla")) {
            handleViolation(data, player, 2, 5, 0.99);
        }
    }
}
