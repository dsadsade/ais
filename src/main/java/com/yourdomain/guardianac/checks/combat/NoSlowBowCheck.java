package com.yourdomain.guardianac.checks.combat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Detects NoSlowBow — moving at full speed while drawing a bow.
 * Vanilla mechanics reduce movement speed to ~20% while charging a bow.
 * NoSlowBow mods bypass this reduction, allowing full speed movement
 * while aiming and charging the bow.
 */
public class NoSlowBowCheck extends Check {

    private static final double MAX_BOW_DRAW_SPEED = 0.13; // blocks/tick, vanilla is ~0.043

    public NoSlowBowCheck(GuardianAC plugin) {
        super(plugin, "NoSlowBow", CheckType.COMBAT);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.BOW || item.getType() == Material.CROSSBOW) {
            if (event.getAction().name().contains("RIGHT")) {
                data.setBowDrawing(true);
            }
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data != null) {
            data.setBowDrawing(false);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (!data.isBowDrawing()) return;
        if (!player.isHandRaised()) {
            data.setBowDrawing(false);
            return;
        }

        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();
        double speed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (speed > MAX_BOW_DRAW_SPEED) {
            handleViolation(data, player, (speed - MAX_BOW_DRAW_SPEED) * 30.0, 10.0, 1.0);
        }
    }
}
