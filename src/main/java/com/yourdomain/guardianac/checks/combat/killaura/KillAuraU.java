package com.yourdomain.guardianac.checks.combat.killaura;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * KillAura (U) — Attack during teleport/respawn invulnerability abuse.
 * Detects players who attack during their invulnerability ticks.
 */
public class KillAuraU extends Check {

    private static final int INVULN_TICKS = 20;

    public KillAuraU(GuardianAC plugin) {
        super(plugin, "KillAura", "U", CheckType.COMBAT);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        int noDamageTicks = player.getNoDamageTicks();
        if (noDamageTicks > INVULN_TICKS) {
            handleViolation(data, player, 6.0, 10.0, 1.0);
        }
    }
}
