package com.yourdomain.guardianac.checks.combat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Detects Criticals hacks — faking critical hits without actually being in the air.
 * Vanilla critical hits require the player to be falling (not on ground, Y velocity negative,
 * not in water, not on ladder, not blind, not in vehicle).
 * Criticals hacks send tiny hop packets to fake the falling condition.
 */
public class CriticalsCheck extends Check {

    public CriticalsCheck(GuardianAC plugin) {
        super(plugin, "Criticals", CheckType.COMBAT);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(event.getPlayer());
        if (data == null) return;

        // Track ground state before attacks
        data.setWasOnGroundBeforeAttack(event.getPlayer().isOnGround());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Check if this was a critical hit
        boolean isCritical = event.isCritical();
        if (!isCritical) return;

        // If they were on ground right before the attack, the critical is suspicious
        // Vanilla criticals require actual falling velocity
        if (data.wasOnGroundBeforeAttack()) {
            // Micro-hop criticals: player was on ground, then suddenly critical
            Location loc = player.getLocation();
            double yMod = loc.getY() - Math.floor(loc.getY());

            // Typical micro-hop: player jumps ~0.04-0.1 blocks
            if (yMod < 0.11 && yMod > 0.001) {
                handleViolation(data, player, 5.0, 10.0, 1.0);
            }
        }

        // Check for underwater criticals (impossible in vanilla)
        if (player.isInWater()) {
            handleViolation(data, player, 8.0, 10.0, 0.5);
        }

        // Check for vehicle criticals (impossible)
        if (player.isInsideVehicle()) {
            handleViolation(data, player, 8.0, 10.0, 0.5);
        }
    }
}
