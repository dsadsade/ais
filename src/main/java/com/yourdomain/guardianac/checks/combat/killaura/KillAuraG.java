package com.yourdomain.guardianac.checks.combat.killaura;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import com.yourdomain.guardianac.utils.PingUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;

/**
 * KillAura (G) — Post-rotation snap-back detection.
 * Detects when a player snaps to a target, attacks, then snaps back to their
 * original look direction. This is a hallmark of silent-aim / rotation hacks.
 */
public class KillAuraG extends Check {

    private static final double SNAPBACK_THRESHOLD = 15.0;

    public KillAuraG(GuardianAC plugin) {
        super(plugin, "KillAura", "G", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Record pre-attack rotation and mark that an attack happened
        data.setPreAttackRotation(data.getLastYaw(), data.getLastPitch());
        data.setAttackedThisTick(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float yaw = event.getTo().getYaw();
        float pitch = event.getTo().getPitch();

        if (data.hasAttackedThisTick()) {
            data.setPostAttackRotation(yaw, pitch);
            data.setAttackedThisTick(false);

            // Compare: pre-attack vs post-attack rotation
            float preYaw = data.getPreAttackYaw();
            float prePitch = data.getPreAttackPitch();
            float postYaw = data.getPostAttackYaw();
            float postPitch = data.getPostAttackPitch();

            // Check if the player snapped back to near their pre-attack look direction
            float yawDiff = Math.abs(wrapAngle(postYaw - preYaw));
            float pitchDiff = Math.abs(postPitch - prePitch);

            // Large attack snap (away from pre-attack direction) then snap back
            float attackYawSnap = Math.abs(wrapAngle(yaw - preYaw));
            if (attackYawSnap > 30.0 && yawDiff < SNAPBACK_THRESHOLD * PingUtil.getRotationMultiplier(player)
                    && pitchDiff < SNAPBACK_THRESHOLD) {
                handleViolation(data, player, 3.0, 6.0, 0.5);
            }
        }

        data.updateRotation(yaw, pitch);
    }

    private float wrapAngle(float angle) {
        angle %= 360f;
        if (angle >= 180f) angle -= 360f;
        if (angle < -180f) angle += 360f;
        return angle;
    }
}
