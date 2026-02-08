package com.yourdomain.guardianac.checks.movement.gravityoverride;

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
 * GravityOverride (B) — No gravity.
 * Flags zero vertical movement while airborne (hover without fly).
 */
public class GravityOverrideB extends Check {

    public GravityOverrideB(GuardianAC plugin) {
        super(plugin, "GravityOverride", "B", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight() || player.isGliding() || player.isInsideVehicle()) return;
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) return;
        if (player.isSwimming()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaY = Math.abs(event.getTo().getY() - event.getFrom().getY());

        if (!player.isOnGround() && data.getAirTicks() > 5 && deltaY < 0.001) {
            handleViolation(data, player, 4.0, 10.0, 2.0);
        }
    }
}
