package com.yourdomain.guardianac.checks.combat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

/**
 * Detects AutoSwitch — automatically switching to the best weapon right before attacking.
 * This mod switches to the highest damage weapon in the exact tick before dealing damage,
 * then switches back. Legitimate players don't switch slots in the same tick as attacking
 * with perfect consistency.
 */
public class AutoSwitchCheck extends Check {

    private static final long SWITCH_ATTACK_THRESHOLD_MS = 50; // switch + attack within 50ms
    private static final int MAX_FAST_SWITCHES = 4; // consecutive fast switch-attacks

    public AutoSwitchCheck(GuardianAC plugin) {
        super(plugin, "AutoSwitch", CheckType.COMBAT);
    }

    @EventHandler
    public void onItemSwitch(PlayerItemHeldEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        data.setLastHeldSlot(event.getNewSlot());
        data.setLastSlotSwitchTime(System.currentTimeMillis());
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
        long timeSinceSwitch = now - data.getLastSlotSwitchTime();

        if (timeSinceSwitch <= SWITCH_ATTACK_THRESHOLD_MS && timeSinceSwitch >= 0) {
            data.setSlotSwitchBeforeAttackCount(data.getSlotSwitchBeforeAttackCount() + 1);

            if (data.getSlotSwitchBeforeAttackCount() >= MAX_FAST_SWITCHES) {
                handleViolation(data, player, 5.0, 10.0, 1.0);
                data.setSlotSwitchBeforeAttackCount(0);
            }
        } else {
            data.setSlotSwitchBeforeAttackCount(Math.max(0, data.getSlotSwitchBeforeAttackCount() - 1));
        }
    }
}
