package com.yourdomain.guardianac.checks.player.badpackets;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class BadPacketsH extends Check {

    public BadPacketsH(GuardianAC plugin) {
        super(plugin, "BadPackets", "H", CheckType.PLAYER);
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        int newSlot = event.getNewSlot();

        // Invalid slot change packets — slot out of valid hotbar range
        if (newSlot < 0 || newSlot > 8) {
            handleViolation(data, player, 10, 5, 0.99);
        }

        long now = System.currentTimeMillis();
        long lastChange = data.getLastSlotChangeTime();

        if (now - lastChange < 15 && lastChange > 0) {
            handleViolation(data, player, 4, 10, 0.995);
        }

        data.setLastSlotChangeTime(now);
    }
}
