package com.yourdomain.guardianac.checks.player.entityaction;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public class EntityActionB extends Check {

    public EntityActionB(GuardianAC plugin) {
        super(plugin, "EntityAction", "B", CheckType.PLAYER);
    }

    @EventHandler
    public void onToggleSprint(PlayerToggleSprintEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        boolean sprinting = event.isSprinting();

        // Sprint state desync — toggling sprint while already in that state
        if (sprinting == data.isSprinting()) {
            handleViolation(data, player, 5, 8, 0.99);
        }

        data.setSprinting(sprinting);
    }
}
