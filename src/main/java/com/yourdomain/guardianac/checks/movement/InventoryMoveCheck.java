package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Detects movement while inventory is open.
 * Vanilla clients cannot send movement packets while an inventory GUI is open.
 * Players using InventoryMove or InventoryWalk hacks can walk while in containers.
 */
public class InventoryMoveCheck extends Check {

    private static final double MOVE_THRESHOLD = 0.15; // ignore tiny drift from server

    public InventoryMoveCheck(GuardianAC plugin) {
        super(plugin, "InventoryMove", CheckType.MOVEMENT);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        data.setInventoryOpen(true);
        data.setInventoryOpenTime(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        data.setInventoryOpen(false);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        if (!data.isInventoryOpen()) return;

        // Allow a short grace period after opening (200ms) for packet desync
        if (System.currentTimeMillis() - data.getInventoryOpenTime() < 200) return;

        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();
        double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (horizontal > MOVE_THRESHOLD) {
            handleViolation(data, player, 4.0, 8.0, 0.5);
        }
    }
}
