package com.yourdomain.guardianac.checks.player.timing;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

public class TimingA extends Check {

    public TimingA(GuardianAC plugin) {
        super(plugin, "Timing", "A", CheckType.PLAYER);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long lastTick = data.getLastTickTime();

        // Server tick abuse detection — packets faster than server tick rate
        if (lastTick > 0) {
            long delta = now - lastTick;
            if (delta < 25) {
                handleViolation(data, player, 4, 12, 0.995);
            }
        }

        data.setLastTickTime(now);
    }
}
