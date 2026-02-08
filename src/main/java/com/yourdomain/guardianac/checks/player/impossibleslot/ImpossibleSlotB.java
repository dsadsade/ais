package com.yourdomain.guardianac.checks.player.impossibleslot;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class ImpossibleSlotB extends Check {

    public ImpossibleSlotB(GuardianAC plugin) {
        super(plugin, "ImpossibleSlot", "B", CheckType.PLAYER);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Armor slot manipulation exploit — clicking armor slots while inventory closed
        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
                long now = System.currentTimeMillis();
                long lastClose = data.getLastInventoryCloseTime();
                if (lastClose > 0 && now - lastClose < 50) {
                    handleViolation(data, player, 8, 5, 0.99);
                }
            }
        }
    }
}
