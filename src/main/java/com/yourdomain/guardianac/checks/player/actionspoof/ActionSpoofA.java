package com.yourdomain.guardianac.checks.player.actionspoof;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

public class ActionSpoofA extends Check {

    public ActionSpoofA(GuardianAC plugin) {
        super(plugin, "ActionSpoof", "A", CheckType.PLAYER);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long lastDigStart = data.getLastDigStartTime();

        // Fake block break — digging state spoof with no dig start
        if (lastDigStart <= 0 || now - lastDigStart < 30) {
            handleViolation(data, player, 7, 5, 0.99);
        }
    }
}
