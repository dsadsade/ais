package com.yourdomain.guardianac.checks.player.autoeat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class AutoEatB extends Check {

    public AutoEatB(GuardianAC plugin) {
        super(plugin, "AutoEat", "B", CheckType.PLAYER);
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        int consecutiveEats = data.incrementConsecutiveEatCount();

        // Perfect food slot selection — rapidly selects and eats food
        if (consecutiveEats > 3) {
            long now = System.currentTimeMillis();
            long lastEat = data.getLastEatTime();
            if (lastEat > 0 && now - lastEat < 2000) {
                handleViolation(data, player, 5, 8, 0.99);
            }
        }

        data.setLastEatTime(System.currentTimeMillis());
    }
}
