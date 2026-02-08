package com.yourdomain.guardianac.checks.combat.sprintreset;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * SprintReset (A) — Frame-perfect W-key release.
 * Detects impossible sprint resets where the player releases and
 * re-presses sprint within 1 tick (0-1 tick gap).
 */
public class SprintResetA extends Check {

    private static final int MAX_PERFECT_RESETS = 5;

    public SprintResetA(GuardianAC plugin) {
        super(plugin, "SprintReset", "A", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        boolean sprinting = player.isSprinting();
        boolean wasSprinting = data.wasSprinting();

        if (!wasSprinting && sprinting) {
            long timeSinceAttack = System.currentTimeMillis() - data.getLastAttackTime();
            if (timeSinceAttack < 60) {
                data.setPerfectSprintResets(data.getPerfectSprintResets() + 1);

                if (data.getPerfectSprintResets() >= MAX_PERFECT_RESETS) {
                    handleViolation(data, player, 4.0, 10.0, 1.0);
                    data.setPerfectSprintResets(0);
                }
            }
        }
    }
}
