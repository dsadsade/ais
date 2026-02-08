package com.yourdomain.guardianac.checks.player.actionspoof;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

public class ActionSpoofB extends Check {

    public ActionSpoofB(GuardianAC plugin) {
        super(plugin, "ActionSpoof", "B", CheckType.PLAYER);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Fake item use — using item without proper use start
        if (player.getItemInHand() == null || player.getItemInHand().getType().isBlock()) return;

        if (!data.isUsingItem()) {
            handleViolation(data, player, 5, 8, 0.99);
        }
    }
}
