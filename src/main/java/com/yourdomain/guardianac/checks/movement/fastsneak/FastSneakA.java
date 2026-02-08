package com.yourdomain.guardianac.checks.movement.fastsneak;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

/**
 * FastSneak (A) — Sneak speed exceeding vanilla.
 * Flags sneaking speed beyond the maximum allowed.
 */
public class FastSneakA extends Check {

    private static final double MAX_SNEAK_SPEED = 0.065;

    public FastSneakA(GuardianAC plugin) {
        super(plugin, "FastSneak", "A", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.isInsideVehicle()) return;
        if (player.hasPotionEffect(PotionEffectType.SPEED)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (!player.isSneaking()) return;

        double horizDist = Math.hypot(event.getTo().getX() - event.getFrom().getX(),
                event.getTo().getZ() - event.getFrom().getZ());

        if (horizDist > MAX_SNEAK_SPEED) {
            handleViolation(data, player, 3.0, 10.0, 1.5);
        }
    }
}
