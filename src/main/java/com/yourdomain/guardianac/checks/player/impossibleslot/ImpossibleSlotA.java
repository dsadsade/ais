package com.yourdomain.guardianac.checks.player.impossibleslot;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class ImpossibleSlotA extends Check {

    public ImpossibleSlotA(GuardianAC plugin) {
        super(plugin, "ImpossibleSlot", "A", CheckType.PLAYER);
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        int newSlot = event.getNewSlot();
        int prevSlot = event.getPreviousSlot();

        // Invalid hotbar slot access — slot index out of bounds
        if (newSlot < 0 || newSlot > 8 || prevSlot < 0 || prevSlot > 8) {
            handleViolation(data, player, 10, 3, 0.99);
        }
    }
}
