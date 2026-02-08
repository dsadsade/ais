package com.yourdomain.guardianac.checks.player.entityaction;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class EntityActionD extends Check {

    public EntityActionD(GuardianAC plugin) {
        super(plugin, "EntityAction", "D", CheckType.PLAYER);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Open inventory action spoof — claiming inventory open while moving fast
        if (data.isInventoryOpen()) {
            double distSq = event.getFrom().distanceSquared(event.getTo());
            if (distSq > 0.5) {
                handleViolation(data, player, 4, 10, 0.995);
            }
        }
    }
}
