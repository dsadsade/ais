package com.yourdomain.guardianac.checks.combat.aimbot;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import com.yourdomain.guardianac.utils.MathUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Aimbot A — detects perfect aim-lock targeting.
 * Aimbot mods lock the player's crosshair exactly onto the target's head/body.
 * This produces unnaturally low angular deviation between the player's look
 * direction and the direction to the target across many consecutive hits.
 * Legitimate players have natural sway and imprecision.
 */
public class AimbotA extends Check {

    private static final float MAX_DEVIATION_THRESHOLD = 1.5f; // degrees — inhumanly low
    private static final int MIN_SAMPLES = 8;

    public AimbotA(GuardianAC plugin) {
        super(plugin, "Aimbot", "A", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location playerEye = player.getEyeLocation();
        Location targetLoc = target.getEyeLocation();

        // Calculate angle between look direction and perfect aim direction
        Vector lookDir = playerEye.getDirection().normalize();
        Vector perfectDir = targetLoc.toVector().subtract(playerEye.toVector()).normalize();

        double dot = lookDir.dot(perfectDir);
        float deviation = (float) Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, dot))));

        data.addAimLockSample(deviation);

        List<Float> samples = data.getAimLockSamples();
        if (samples.size() >= MIN_SAMPLES) {
            // Calculate average deviation across recent hits
            double[] arr = new double[samples.size()];
            for (int i = 0; i < samples.size(); i++) arr[i] = samples.get(i);

            double avg = 0;
            for (double v : arr) avg += v;
            avg /= arr.length;
            double stdDev = MathUtil.standardDeviation(arr);

            // Perfect aim = very low average AND very low deviation (no sway)
            if (avg < MAX_DEVIATION_THRESHOLD && stdDev < 1.0) {
                data.setPerfectAimCount(data.getPerfectAimCount() + 1);

                if (data.getPerfectAimCount() >= 3) {
                    handleViolation(data, player, 5.0, 10.0, 0.5);
                    data.setPerfectAimCount(0);
                }
            } else {
                data.setPerfectAimCount(Math.max(0, data.getPerfectAimCount() - 1));
            }
        }
    }
}
