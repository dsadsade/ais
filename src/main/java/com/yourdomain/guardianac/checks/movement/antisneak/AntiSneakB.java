package com.yourdomain.guardianac.checks.movement.antisneak;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

/**
 * AntiSneak (B) — No sneak model change.
 * Flags rapid sneak toggle without sustaining sneak pose.
 */
public class AntiSneakB extends Check {

    private int rapidToggleCount;
    private long lastToggleTime;

    public AntiSneakB(GuardianAC plugin) {
        super(plugin, "AntiSneak", "B", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onToggleSneak(PlayerToggleSneakEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isInsideVehicle()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        if (now - lastToggleTime < 100) {
            rapidToggleCount++;
            if (rapidToggleCount > 6) {
                handleViolation(data, player, 3.0, 10.0, 1.5);
                rapidToggleCount = 0;
            }
        } else {
            rapidToggleCount = Math.max(0, rapidToggleCount - 1);
        }

        lastToggleTime = now;
    }
}
