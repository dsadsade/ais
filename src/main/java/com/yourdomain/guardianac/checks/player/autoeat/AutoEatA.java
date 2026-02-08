package com.yourdomain.guardianac.checks.player.autoeat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class AutoEatA extends Check {

    public AutoEatA(GuardianAC plugin) {
        super(plugin, "AutoEat", "A", CheckType.PLAYER);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long eatStart = data.getEatStartTime();

        // Instant food consumption start — eating finished too quickly
        if (eatStart > 0 && now - eatStart < 1500) {
            handleViolation(data, player, 6, 5, 0.99);
        }
    }
}
