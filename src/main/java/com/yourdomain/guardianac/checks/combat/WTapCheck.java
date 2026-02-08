package com.yourdomain.guardianac.checks.combat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Detects impossible W-Tap combos — perfect sprint-reset on every single hit.
 * W-tapping is a legitimate PvP technique (releasing W to reset sprint), but
 * doing it frame-perfectly on every hit with machine-like consistency indicates
 * an automated W-tap mod. This check flags players who toggle sprint at
 * inhuman rates surrounding each attack.
 */
public class WTapCheck extends Check {

    private static final int MAX_PERFECT_WTAPS = 6; // consecutive perfect W-taps in window
    private static final long WTAP_WINDOW_MS = 5000;
    private static final long SPRINT_TOGGLE_THRESHOLD_MS = 80; // toggle <80ms before hit

    public WTapCheck(GuardianAC plugin) {
        super(plugin, "WTap", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long timeSinceSprintToggle = now - data.getLastSprintToggleTime();

        // Check if sprint was toggled very close to the attack
        if (timeSinceSprintToggle <= SPRINT_TOGGLE_THRESHOLD_MS && player.isSprinting()) {
            // Reset window if expired
            if (now - data.getWTapWindowStart() > WTAP_WINDOW_MS) {
                data.setWTapCount(0);
                data.setWTapWindowStart(now);
            }

            data.setWTapCount(data.getWTapCount() + 1);

            if (data.getWTapCount() >= MAX_PERFECT_WTAPS) {
                handleViolation(data, player, 5.0, 10.0, 0.5);
                data.setWTapCount(0);
            }
        }
    }
}
