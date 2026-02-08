package com.yourdomain.guardianac.checks.combat.killaura;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.PingUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * KillAura (A) — Rotation snap detection.
 * Detects sudden, large rotation changes on the exact tick a player attacks.
 * Legitimate players smoothly aim; aura clients snap to the target instantly.
 */
public class KillAuraA extends Check {

    public KillAuraA(GuardianAC plugin) {
        super(plugin, "KillAura", "A", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float yaw = player.getLocation().getYaw();
        float pitch = player.getLocation().getPitch();
        float deltaYaw = Math.abs(wrapAngle(yaw - data.getLastYaw()));
        float deltaPitch = Math.abs(pitch - data.getLastPitch());

        // Large snap on the attack tick (scaled for ping)
        double rotMult = PingUtil.getRotationMultiplier(player);
        double yawThreshold = 35.0 * rotMult;
        double pitchThreshold = 25.0 * rotMult;

        if (deltaYaw > yawThreshold && deltaPitch > pitchThreshold) {
            long timeSinceLastAttack = System.currentTimeMillis() - data.getLastAttackTime();
            if (timeSinceLastAttack < PingUtil.adjustTimeWindow(player, 150)) {
                handleViolation(data, player, 2.5, 6.0, 0.5);
            }
        }

        data.setLastAttackTime(System.currentTimeMillis());
    }

    private float wrapAngle(float angle) {
        angle %= 360f;
        if (angle >= 180f) angle -= 360f;
        if (angle < -180f) angle += 360f;
        return angle;
    }
}
