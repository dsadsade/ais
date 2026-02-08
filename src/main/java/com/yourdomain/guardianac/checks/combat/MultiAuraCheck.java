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

import java.util.Set;

/**
 * Detects MultiAura hacks — hitting multiple distinct entities in the same server tick.
 * Vanilla Minecraft can only hit one entity per attack cooldown sweep.
 * MultiAura or ForceField hacks target many entities simultaneously.
 */
public class MultiAuraCheck extends Check {

    private static final int MAX_ENTITIES_PER_TICK = 1; // vanilla max
    private static final long TICK_WINDOW_MS = 55; // ~1 tick plus small tolerance

    public MultiAuraCheck(GuardianAC plugin) {
        super(plugin, "MultiAura", CheckType.COMBAT);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        Set<Integer> entitiesThisTick = data.getAttackedEntitiesThisTick();

        // Reset if new tick
        if (now - data.getLastAttackTickTime() > TICK_WINDOW_MS) {
            entitiesThisTick.clear();
            data.setLastAttackTickTime(now);
        }

        entitiesThisTick.add(target.getEntityId());

        // Multiple different entities hit within the same tick window
        if (entitiesThisTick.size() > MAX_ENTITIES_PER_TICK) {
            handleViolation(data, player, entitiesThisTick.size() * 3.0, 10.0, 1.0);
        }
    }
}
