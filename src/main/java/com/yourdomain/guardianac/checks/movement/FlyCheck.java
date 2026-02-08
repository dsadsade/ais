package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

/**
 * Detects Fly hacks with multiple sub-checks:
 * (A) Sustained air time without normal gravity deceleration
 * (B) Hovering — maintaining the same Y level in the air
 * (C) Ascending without proper jump physics
 */
public class FlyCheck extends Check {

    private static final int MAX_AIR_TICKS = 80; // ~4 seconds of air time max (accounting for high jumps)
    private static final double GRAVITY_DECELERATION = 0.08; // vanilla gravity per tick
    private static final int HOVER_TICK_THRESHOLD = 15; // how many ticks of zero Y change is suspicious

    public FlyCheck(GuardianAC plugin) {
        super(plugin, "Fly", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.hasPotionEffect(PotionEffectType.LEVITATION)) return;
        if (player.hasPotionEffect(PotionEffectType.SLOW_FALLING)) return;
        if (player.isGliding()) return; // Elytra
        if (player.isInsideVehicle()) return;
        if (player.isSwimming()) return;
        if (player.isRiptiding()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        double deltaY = to.getY() - from.getY();

        if (player.isOnGround()) {
            data.setAirTicks(0);
            data.setLastGroundLocation(player.getLocation());
            data.setLastOnGroundTime(System.currentTimeMillis());
            data.setHoverTicks(0);
            return;
        }

        data.incrementAirTicks();
        data.setLastDeltaY(deltaY);

        // Check A: Excessive air time
        if (data.getAirTicks() > MAX_AIR_TICKS) {
            if (!hasSolidBlockNearby(player.getLocation()) && !isAboveLiquid(player.getLocation())) {
                flag(data);
                return;
            }
        }

        // Check B: Hovering — staying at the same Y level in the air
        if (Math.abs(deltaY) < 0.005 && data.getAirTicks() > 5) {
            data.setHoverTicks(data.getHoverTicks() + 1);
            if (data.getHoverTicks() > HOVER_TICK_THRESHOLD) {
                if (!isNearClimbable(player.getLocation())) {
                    flag(data);
                    data.setHoverTicks(0);
                    return;
                }
            }
        } else {
            data.setHoverTicks(0);
        }

        // Check C: Ascending in the air without jump velocity pattern
        // Normal jump: initial velocity ~0.42, then decelerates by 0.08 each tick
        if (deltaY > 0 && data.getAirTicks() > 10) {
            // If still going up after 10 air ticks with no support, that's suspicious
            if (!hasSolidBlockNearby(player.getLocation())) {
                flag(data);
            }
        }
    }

    private boolean hasSolidBlockNearby(Location location) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -2; y <= 0; y++) {
                    Block block = location.clone().add(x, y, z).getBlock();
                    if (block.getType().isSolid()) return true;
                }
            }
        }
        return false;
    }

    private boolean isAboveLiquid(Location location) {
        for (int y = -1; y >= -3; y--) {
            Material type = location.clone().add(0, y, 0).getBlock().getType();
            if (type == Material.WATER || type == Material.LAVA) return true;
            if (type.isSolid()) break;
        }
        return false;
    }

    private boolean isNearClimbable(Location location) {
        Material type = location.getBlock().getType();
        return type == Material.LADDER
                || type == Material.VINE
                || type == Material.SCAFFOLDING
                || type == Material.TWISTING_VINES
                || type == Material.WEEPING_VINES
                || type == Material.CAVE_VINES;
    }
}
