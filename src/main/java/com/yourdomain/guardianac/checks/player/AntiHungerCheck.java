package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Detects AntiHunger hacks — preventing hunger from depleting during movement.
 * Vanilla hunger decreases when sprinting, jumping, swimming, taking damage, etc.
 * AntiHunger mods send spoofed packets to prevent the server from reducing food level.
 * Detected by monitoring if food level stays static during sustained sprinting.
 */
public class AntiHungerCheck extends Check {

    private static final int STALE_HUNGER_THRESHOLD = 200; // ticks of sprinting with no hunger loss
    private static final int MIN_FOOD_LEVEL_CHECK = 18; // only suspicious below full

    public AntiHungerCheck(GuardianAC plugin) {
        super(plugin, "AntiHunger", CheckType.PLAYER);
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Food level changed — reset stale counter
        data.setStaleHungerTicks(0);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;
        if (!player.isSprinting()) return;
        if (player.getFoodLevel() >= 20) return; // full hunger, won't deplete as fast

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Only track meaningful movement
        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();
        double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (horizontal < 0.1) return;

        data.setStaleHungerTicks(data.getStaleHungerTicks() + 1);

        if (data.getStaleHungerTicks() >= STALE_HUNGER_THRESHOLD) {
            if (player.getFoodLevel() < MIN_FOOD_LEVEL_CHECK) {
                handleViolation(data, player, 3.0, 12.0, 0.5);
            }
            data.setStaleHungerTicks(0);
        }
    }
}
