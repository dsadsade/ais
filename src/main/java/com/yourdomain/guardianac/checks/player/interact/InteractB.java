package com.yourdomain.guardianac.checks.player.interact;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractB extends Check {

    public InteractB(GuardianAC plugin) {
        super(plugin, "Interact", "B", CheckType.PLAYER);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;
        if (event.getClickedBlock() == null) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Interaction through walls — no line of sight to target block
        if (!player.hasLineOfSight(event.getClickedBlock().getLocation().add(0.5, 0.5, 0.5))) {
            handleViolation(data, player, 6, 8, 0.99);
        }
    }
}
