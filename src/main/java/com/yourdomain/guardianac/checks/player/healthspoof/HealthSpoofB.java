package com.yourdomain.guardianac.checks.player.healthspoof;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class HealthSpoofB extends Check {

    public HealthSpoofB(GuardianAC plugin) {
        super(plugin, "HealthSpoof", "B", CheckType.PLAYER);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double damage = event.getFinalDamage();
        double healthBefore = player.getHealth();

        // Damage negation — taking damage but no health change expected
        if (damage > 0 && event.isCancelled()) return;

        if (damage > 0 && healthBefore == data.getLastRecordedHealth() && data.getLastDamageTime() > 0) {
            long elapsed = System.currentTimeMillis() - data.getLastDamageTime();
            if (elapsed < 500) {
                handleViolation(data, player, 7, 5, 0.99);
            }
        }

        data.setLastRecordedHealth(healthBefore);
    }
}
