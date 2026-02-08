package com.yourdomain.guardianac.checks.player.inventory;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryB extends Check {

    public InventoryB(GuardianAC plugin) {
        super(plugin, "Inventory", "B", CheckType.PLAYER);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        int slot = event.getRawSlot();
        int invSize = event.getInventory().getSize();

        // Invalid slot access — slot index out of inventory bounds
        if (slot < 0 || slot >= invSize + 36) {
            handleViolation(data, player, 10, 3, 0.99);
        }
    }
}
