package com.yourdomain.guardianac.checks.combat.nohitdelay;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * NoHitDelay (B) — Tick timing reset detection.
 * Checks the entity hurt time (noDamageTicks) to ensure vanilla
 * invulnerability frames are being respected.
 */
public class NoHitDelayB extends Check {

    private static final int MIN_NO_DAMAGE_TICKS = 10;

    public NoHitDelayB(GuardianAC plugin) {
        super(plugin, "NoHitDelay", "B", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        int noDamageTicks = target.getNoDamageTicks();
        long lastHurtTime = data.getLastEntityHurtTime();

        if (noDamageTicks > 0 && noDamageTicks < MIN_NO_DAMAGE_TICKS) {
            if (lastHurtTime > 0 && lastHurtTime < MIN_NO_DAMAGE_TICKS) {
                handleViolation(data, player, 5.0, 10.0, 1.0);
            }
        }

        data.setLastEntityHurtTime(noDamageTicks);
    }
}
