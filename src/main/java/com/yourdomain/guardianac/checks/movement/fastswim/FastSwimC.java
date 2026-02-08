package com.yourdomain.guardianac.checks.movement.fastswim;

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
 * FastSwim (C) — Dolphin's grace exploitation.
 * Flags excessive speed even with Dolphin's Grace (beyond buffed max).
 */
public class FastSwimC extends Check {

    private static final double MAX_DOLPHIN_SPEED = 0.35;

    public FastSwimC(GuardianAC plugin) {
        super(plugin, "FastSwim", "C", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle() || player.isFlying()) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (!player.isSwimming() || !player.hasPotionEffect(PotionEffectType.DOLPHINS_GRACE)) return;

        double horizDist = Math.hypot(event.getTo().getX() - event.getFrom().getX(),
                event.getTo().getZ() - event.getFrom().getZ());

        if (horizDist > MAX_DOLPHIN_SPEED) {
            handleViolation(data, player, 3.0, 12.0, 1.5);
        }
    }
}
