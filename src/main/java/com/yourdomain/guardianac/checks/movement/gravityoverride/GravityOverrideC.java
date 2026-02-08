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
 * GravityOverride (C) — Reversed gravity.
 * Flags ascending without a jump while airborne (going up without jump input).
 */
public class GravityOverrideC extends Check {

    public GravityOverrideC(GuardianAC plugin) {
        super(plugin, "GravityOverride", "C", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight() || player.isGliding() || player.isInsideVehicle()) return;
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) return;
        if (player.isSwimming() || player.isRiptiding()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        // After several air ticks, ascending is impossible without an external force
        if (!player.isOnGround() && data.getAirTicks() > 8 && deltaY > 0.1) {
            handleViolation(data, player, 5.0, 10.0, 2.0);
        }
    }
}
