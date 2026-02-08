package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.vehicle.VehicleMoveEvent;

/**
 * Detects EntitySpeed hacks — making ridden entities (horses, pigs, striders)
 * move faster than their maximum allowed speed. Speed hacks applied to mounts
 * allow players to travel much faster than intended.
 */
public class EntitySpeedCheck extends Check {

    private static final double HORSE_MAX_BASE = 0.45;  // max horse speed attribute in vanilla (14.23 blocks/s)
    private static final double PIG_MAX_SPEED = 0.18;   // ~5.0 blocks/s with carrot on stick
    private static final double STRIDER_MAX_SPEED = 0.2; // ~5.7 blocks/s with warped fungus rod
    private static final double TOLERANCE = 1.3; // 30% tolerance for lag

    public EntitySpeedCheck(GuardianAC plugin) {
        super(plugin, "EntitySpeed", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onVehicleMove(VehicleMoveEvent event) {
        if (!isEnabled()) return;

        Entity vehicle = event.getVehicle();
        if (vehicle.getPassengers().isEmpty()) return;
        if (!(vehicle.getPassengers().get(0) instanceof Player player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;
        if (isExempt(player)) return;

        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();
        double speed = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double maxSpeed;

        if (vehicle instanceof Horse horse) {
            double baseSpeed = horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getValue();
            maxSpeed = baseSpeed * 43.17 / 20.0; // convert to blocks per tick
            maxSpeed = Math.max(maxSpeed, HORSE_MAX_BASE);
        } else if (vehicle instanceof Pig) {
            maxSpeed = PIG_MAX_SPEED;
        } else if (vehicle instanceof Strider) {
            maxSpeed = STRIDER_MAX_SPEED;
        } else if (vehicle instanceof Boat) {
            return; // BoatFly handles boats
        } else {
            maxSpeed = 0.5; // generic entity speed limit
        }

        maxSpeed *= TOLERANCE;

        if (speed > maxSpeed) {
            double excess = speed - maxSpeed;
            handleViolation(data, player, excess * 5.0, 10.0, 1.0);
        }
    }
}
