package com.yourdomain.guardianac.checks.player.entityaction;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class EntityActionA extends Check {

    public EntityActionA(GuardianAC plugin) {
        super(plugin, "EntityAction", "A", CheckType.PLAYER);
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        boolean sneaking = event.isSneaking();

        // Sneak state desync — toggling sneak while already in that state
        if (sneaking == data.isSneaking()) {
            handleViolation(data, player, 5, 8, 0.99);
        }

        data.setSneaking(sneaking);
    }
}
