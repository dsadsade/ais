package com.yourdomain.guardianac.checks.player.interact;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

public class InteractC extends Check {

    public InteractC(GuardianAC plugin) {
        super(plugin, "Interact", "C", CheckType.PLAYER);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long lastInteract = data.getLastInteractTime();

        // Interact rate limiting — excessive interaction rate
        if (lastInteract > 0 && now - lastInteract < 25) {
            handleViolation(data, player, 3, 15, 0.995);
        }

        data.setLastInteractTime(now);
    }
}
