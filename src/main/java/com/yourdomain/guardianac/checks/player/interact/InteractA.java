package com.yourdomain.guardianac.checks.player.interact;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class InteractA extends Check {

    public InteractA(GuardianAC plugin) {
        super(plugin, "Interact", "A", CheckType.PLAYER);
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double distance = player.getLocation().distance(event.getRightClicked().getLocation());

        // Invalid entity interaction distance
        if (distance > 6.0) {
            handleViolation(data, player, 8, 5, 0.99);
        }
    }
}
