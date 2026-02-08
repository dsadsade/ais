package com.yourdomain.guardianac.checks.combat.aimbot;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Aimbot (H) — Inhuman aim snap speed detection.
 * Detects rotation snaps that exceed humanly possible mouse movement,
 * especially from a near-still state to a massive rotation on attack tick.
 */
public class AimbotH extends Check {

    private static final float MAX_SNAP = 90.0f;

    public AimbotH(GuardianAC plugin) {
        super(plugin, "Aimbot", "H", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float yawDelta = Math.abs(player.getLocation().getYaw() - data.getLastYaw());
        if (yawDelta > 180) yawDelta = 360 - yawDelta;
        float pitchDelta = Math.abs(player.getLocation().getPitch() - data.getLastPitch());

        float totalSnap = (float) Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);

        float prevYawDelta = Math.abs(data.getLastYaw() - data.getPrevLastYaw());
        if (prevYawDelta > 180) prevYawDelta = 360 - prevYawDelta;
        float prevPitchDelta = Math.abs(data.getLastPitch() - data.getPrevLastPitch());
        float prevTotal = (float) Math.sqrt(prevYawDelta * prevYawDelta + prevPitchDelta * prevPitchDelta);

        if (totalSnap > MAX_SNAP && prevTotal < 5.0f) {
            handleViolation(data, player, 6.0, 10.0, 0.5);
        }
    }
}
