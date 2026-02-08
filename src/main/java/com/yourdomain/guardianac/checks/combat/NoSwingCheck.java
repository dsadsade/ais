package com.yourdomain.guardianac.checks.combat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;

/**
 * Detects NoSwing hacks — hitting entities without swinging the arm.
 * Vanilla Minecraft sends an arm swing animation packet before each attack.
 * NoSwing hacks suppress the animation while still dealing damage.
 */
public class NoSwingCheck extends Check {

    private static final long SWING_TOLERANCE_MS = 200; // max time between swing and hit

    public NoSwingCheck(GuardianAC plugin) {
        super(plugin, "NoSwing", CheckType.COMBAT);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAnimation(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        data.setLastSwingTime(System.currentTimeMillis());
        data.setPendingSwing(true);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long lastSwing = data.getLastSwingTime();
        long timeSinceSwing = now - lastSwing;

        // If no swing occurred within the tolerance window before the hit
        if (timeSinceSwing > SWING_TOLERANCE_MS || lastSwing == 0) {
            handleViolation(data, player, 4.0, 10.0, 1.0);
        }

        data.setPendingSwing(false);
    }
}
