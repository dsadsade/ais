package com.yourdomain.guardianac.checks.player.healthspoof;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class HealthSpoofA extends Check {

    public HealthSpoofA(GuardianAC plugin) {
        super(plugin, "HealthSpoof", "A", CheckType.PLAYER);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double healthBefore = player.getHealth();
        double expectedAfter = healthBefore - event.getFinalDamage();

        // Health value desync detection — store for post-check
        data.setExpectedHealth(Math.max(0, expectedAfter));
        data.setLastDamageTime(System.currentTimeMillis());
    }
}
