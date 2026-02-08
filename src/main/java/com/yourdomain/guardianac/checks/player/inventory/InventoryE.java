package com.yourdomain.guardianac.checks.player.inventory;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryE extends Check {

    public InventoryE(GuardianAC plugin) {
        super(plugin, "Inventory", "E", CheckType.PLAYER);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (event.getClick() != ClickType.SHIFT_LEFT && event.getClick() != ClickType.SHIFT_RIGHT) return;

        long now = System.currentTimeMillis();
        long lastShiftClick = data.getLastShiftClickTime();

        // Shift-click speed analysis — inhuman shift-click rate
        if (lastShiftClick > 0 && now - lastShiftClick < 30) {
            handleViolation(data, player, 5, 10, 0.995);
        }

        data.setLastShiftClickTime(now);
    }
}
