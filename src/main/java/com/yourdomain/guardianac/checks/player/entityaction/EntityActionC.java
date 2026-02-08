package com.yourdomain.guardianac.checks.player.entityaction;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class EntityActionC extends Check {

    public EntityActionC(GuardianAC plugin) {
        super(plugin, "EntityAction", "C", CheckType.PLAYER);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Horse jumping while not on horse — jump boost packet without vehicle
        if (data.isHorseJumping() && !player.isInsideVehicle()) {
            handleViolation(data, player, 8, 5, 0.99);
        }
    }
}
