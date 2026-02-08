package com.yourdomain.guardianac.checks.player.inventory;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryA extends Check {

    public InventoryA(GuardianAC plugin) {
        super(plugin, "Inventory", "A", CheckType.PLAYER);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Click while closed inventory — no open inventory view
        if (player.getOpenInventory().getTopInventory() == null) {
            handleViolation(data, player, 8, 5, 0.99);
        }
    }
}
