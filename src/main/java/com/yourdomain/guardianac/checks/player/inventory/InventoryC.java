package com.yourdomain.guardianac.checks.player.inventory;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryC extends Check {

    public InventoryC(GuardianAC plugin) {
        super(plugin, "Inventory", "C", CheckType.PLAYER);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long lastClick = data.getLastInventoryClickTime();

        // Item move speed — clicks too fast for human input
        if (lastClick > 0 && now - lastClick < 20) {
            handleViolation(data, player, 5, 12, 0.995);
        }

        data.setLastInventoryClickTime(now);
    }
}
