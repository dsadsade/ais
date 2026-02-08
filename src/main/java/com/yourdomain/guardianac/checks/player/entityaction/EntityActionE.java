package com.yourdomain.guardianac.checks.player.entityaction;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class EntityActionE extends Check {

    public EntityActionE(GuardianAC plugin) {
        super(plugin, "EntityAction", "E", CheckType.PLAYER);
    }

    @EventHandler
    public void onBedLeave(PlayerBedLeaveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Bed leave while not in bed — spoofed bed leave action
        if (!data.isInBed()) {
            handleViolation(data, player, 8, 5, 0.99);
        }

        data.setInBed(false);
    }
}
