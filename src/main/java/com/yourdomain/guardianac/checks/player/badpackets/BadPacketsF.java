package com.yourdomain.guardianac.checks.player.badpackets;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class BadPacketsF extends Check {

    public BadPacketsF(GuardianAC plugin) {
        super(plugin, "BadPackets", "F", CheckType.PLAYER);
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

        // Invalid entity action packets sent too rapidly
        if (now - lastAction < 10 && lastAction > 0) {
            handleViolation(data, player, 4, 8, 0.99);
        }

        data.setLastEntityActionTime(now);
    }
}
