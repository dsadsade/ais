package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

/**
 * Detects NoFall hacks. When a player falls a significant distance,
 * they should take fall damage. If they consistently do not, they are likely cheating.
 * Tracks vertical movement and cross-references with fall damage events.
 */
public class NoFallCheck extends Check {

    private static final double MIN_FALL_DISTANCE_FOR_DAMAGE = 3.5; // blocks
    private static final long GRACE_PERIOD_MS = 200;

    public NoFallCheck(GuardianAC plugin) {
        super(plugin, "NoFall", CheckType.PLAYER);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        if (player.isOnGround()) {
            data.setLastOnGroundTime(System.currentTimeMillis());
            data.setLastGroundLocation(player.getLocation());
            data.setAirTicks(0);
        } else {
            data.incrementAirTicks();
        }

        data.setLastDeltaY(deltaY);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // If the player claims to be on the ground but should have taken fall damage from
        // a significant height, and the damage is cancelled/zero, flag them
        if (event.isCancelled() || event.getDamage() <= 0) {
            double fallDistance = player.getFallDistance();
            if (fallDistance >= MIN_FALL_DISTANCE_FOR_DAMAGE) {
                // Check for legitimate reasons to avoid fall damage
                if (!hasLegitFallProtection(player)) {
                    flag(data);
                }
            }
        }
    }

    private boolean hasLegitFallProtection(Player player) {
        // Check for feather falling enchantment
        if (player.getInventory().getBoots() != null &&
                player.getInventory().getBoots().containsEnchantment(org.bukkit.enchantments.Enchantment.FEATHER_FALLING)) {
            return true; // feather falling reduces damage, doesn't negate it; still might be suspicious
        }

        // Check for slime block or bed below
        Material below = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        if (below == Material.SLIME_BLOCK || below == Material.HAY_BLOCK) {
            return true;
        }

        // Check for water at landing location
        Material at = player.getLocation().getBlock().getType();
        if (at == Material.WATER || at == Material.LAVA) {
            return true;
        }

        // Riptide trident / elytra
        if (player.isGliding()) return true;

        // Totem of undying
        if (player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING
                || player.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING) {
            return true;
        }

        return false;
    }
}
