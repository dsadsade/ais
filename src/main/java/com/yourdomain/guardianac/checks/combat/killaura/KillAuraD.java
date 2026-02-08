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
 * KillAura (D) — Multi-aura / rapid target switching.
 * Detects rapidly switching between multiple attack targets in a short window.
 * Legitimate PvP involves focusing one target; multi-aura cycles through many.
 */
public class KillAuraD extends Check {

    private static final long SWITCH_WINDOW_MS = 3000;
    private static final int MAX_SWITCHES_NORMAL = 4;
    private static final int MAX_SWITCHES_SUSPICIOUS = 6;

    public KillAuraD(GuardianAC plugin) {
        super(plugin, "KillAura", "D", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        int targetId = target.getEntityId();
        long windowMs = PingUtil.adjustTimeWindow(player, SWITCH_WINDOW_MS);

        // Reset window if expired
        if (now - data.getTargetSwitchWindowStart() > windowMs) {
            data.resetTargetSwitchCount();
            data.setTargetSwitchWindowStart(now);
        }

        // Detect target switch
        if (data.getLastTargetEntityId() != -1 && data.getLastTargetEntityId() != targetId) {
            data.incrementTargetSwitchCount();

            int switches = data.getTargetSwitchCount();
            if (switches >= MAX_SWITCHES_SUSPICIOUS) {
                handleViolation(data, player, 3.0, 6.0, 0.5);
            } else if (switches >= MAX_SWITCHES_NORMAL) {
                handleViolation(data, player, 1.0, 6.0, 0.5);
            }
        }

        data.setLastTargetEntityId(targetId);
        data.setLastAttackTime(now);
    }
}
