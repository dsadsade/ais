package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

/**
 * Detects NoSlow hacks. When a player is using an item (eating, blocking with a shield,
 * drawing a bow), they should be slowed down significantly.
 * If their speed is too high while using an item, they are likely cheating.
 */
public class NoSlowCheck extends Check {

    // Normal walking speed is ~0.222; when using an item it should be ~0.065
    private static final double MAX_SPEED_WHILE_USING = 0.15;

    public NoSlowCheck(GuardianAC plugin) {
        super(plugin, "NoSlow", CheckType.PLAYER);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Check if the player is actively using an item (hand raised)
        if (!player.isHandRaised()) {
            data.setUsingItem(false);
            return;
        }

        data.setUsingItem(true);

        // Ensure the item being used actually slows the player
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        ItemStack offHand = player.getInventory().getItemInOffHand();
        if (!isSlowingItem(mainHand) && !isSlowingItem(offHand)) return;

        double speed = event.getFrom().toVector().setY(0).distance(event.getTo().toVector().setY(0));

        double maxSpeed = MAX_SPEED_WHILE_USING;

        // Account for speed potion effect
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int amplifier = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier();
            maxSpeed *= (1 + 0.2 * (amplifier + 1));
        }

        // Ice blocks increase speed
        Material below = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        if (below == Material.ICE || below == Material.PACKED_ICE || below == Material.BLUE_ICE) {
            maxSpeed *= 1.5;
        }

        // Small buffer for network jitter
        maxSpeed += 0.03;

        if (speed > maxSpeed) {
            flag(data);
        }
    }

    private boolean isSlowingItem(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type.isEdible()
                || type == Material.SHIELD
                || type == Material.BOW
                || type == Material.CROSSBOW
                || type == Material.TRIDENT
                || type == Material.SPYGLASS;
    }
}
