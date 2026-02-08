package com.yourdomain.guardianac.checks.combat.sprintreset;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * SprintReset (B) — Direction toggle for sprint reset.
 * Detects backwards-forward movement toggles used for sprint resets,
 * checking if the player reverses direction mid-combo.
 */
public class SprintResetB extends Check {

    private static final int TOGGLE_THRESHOLD = 4;

    public SprintResetB(GuardianAC plugin) {
        super(plugin, "SprintReset", "B", CheckType.COMBAT);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long timeSinceAttack = System.currentTimeMillis() - data.getLastAttackTime();
        if (timeSinceAttack > 500) return;

        double dx = event.getTo().getX() - event.getFrom().getX();
        double dz = event.getTo().getZ() - event.getFrom().getZ();

        float yaw = player.getLocation().getYaw();
        double forwardX = -Math.sin(Math.toRadians(yaw));
        double forwardZ = Math.cos(Math.toRadians(yaw));

        double dot = dx * forwardX + dz * forwardZ;

        boolean movingBackward = dot < -0.01;
        boolean wasSprinting = data.wasSprinting();

        if (movingBackward && wasSprinting) {
            data.setSprintToggleCount(data.getSprintToggleCount() + 1);

            if (data.getSprintToggleCount() >= TOGGLE_THRESHOLD) {
                handleViolation(data, player, 4.0, 10.0, 1.0);
                data.setSprintToggleCount(0);
            }
        }
    }
}
