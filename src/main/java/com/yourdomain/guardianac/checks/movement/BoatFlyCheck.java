package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleMoveEvent;

/**
 * Detects BoatFly hacks — boats ascending or flying in the air without liquid support.
 * Boats should only move upward in water or when hitting waves. Sustained upward
 * movement without water below is an exploit.
 */
public class BoatFlyCheck extends Check {

    private static final double MAX_BOAT_ASCEND = 0.5; // per tick
    private static final int FLAG_THRESHOLD = 5;

    public BoatFlyCheck(GuardianAC plugin) {
        super(plugin, "BoatFly", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!isEnabled()) return;
        if (!(event.getVehicle() instanceof Boat boat)) return;
        if (boat.getPassengers().isEmpty()) return;
        if (!(boat.getPassengers().get(0) instanceof Player player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;
        if (isExempt(player)) return;

        double deltaY = event.getTo().getY() - event.getFrom().getY();

        // Boats should not be ascending without water
        if (deltaY > MAX_BOAT_ASCEND) {
            // Check if boat is on water
            if (!boat.isInWater() && !event.getFrom().getBlock().isLiquid()) {
                handleViolation(data, player, 4.0, 10.0, 1.0);
            }
        }

        // Check for sustained flight (boat at same Y in air)
        if (!boat.isOnGround() && !boat.isInWater() && Math.abs(deltaY) < 0.01) {
            double horizontal = Math.sqrt(
                    Math.pow(event.getTo().getX() - event.getFrom().getX(), 2) +
                    Math.pow(event.getTo().getZ() - event.getFrom().getZ(), 2));
            if (horizontal > 0.1) {
                handleViolation(data, player, 3.0, 8.0, 0.5);
            }
        }
    }
}
