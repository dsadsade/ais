package com.yourdomain.guardianac.checks.combat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Detects impossible weapon switching speed during combat — changing
 * hotbar slot and immediately hitting within the same tick.
 * Common with auto-weapon/auto-sword modules.
 */
public class WeaponSwitchCheck extends Check {

    private static final long MIN_SWITCH_TO_HIT_MS = 30;
    private final Map<UUID, Long> lastSlotSwitch = new ConcurrentHashMap<>();

    public WeaponSwitchCheck(GuardianAC plugin) {
        super(plugin, "WeaponSwitch", CheckType.COMBAT);
    }

    @EventHandler
    public void onSlotSwitch(PlayerItemHeldEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        lastSlotSwitch.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (isExempt(player)) return;

        Long lastSwitch = lastSlotSwitch.get(player.getUniqueId());
        if (lastSwitch == null) return;

        long delta = System.currentTimeMillis() - lastSwitch;
        if (delta < MIN_SWITCH_TO_HIT_MS && delta >= 0) {
            flag(player, String.format("switch-to-hit=%dms min=%dms", delta, MIN_SWITCH_TO_HIT_MS));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastSlotSwitch.remove(event.getPlayer().getUniqueId());
    }
}
