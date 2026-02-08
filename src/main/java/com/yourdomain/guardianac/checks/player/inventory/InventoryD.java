package com.yourdomain.guardianac.checks.player.inventory;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryD extends Check {

    public InventoryD(GuardianAC plugin) {
        super(plugin, "Inventory", "D", CheckType.PLAYER);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Creative mode item injection in survival
        if (player.getGameMode() == GameMode.SURVIVAL
                && event.getClickedInventory() != null
                && event.getClickedInventory().getType() == InventoryType.CREATIVE) {
            handleViolation(data, player, 10, 3, 0.99);
        }
    }
}
