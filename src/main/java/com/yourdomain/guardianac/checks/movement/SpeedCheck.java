package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

public class SpeedCheck extends Check {

    public SpeedCheck(GuardianAC plugin) {
        super(plugin, "Speed", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        PlayerData playerData = getPlugin().getPlayerDataManager().getPlayerData(player);

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || player.isFlying() || player.getAllowFlight()) {
            return;
        }

        double speed = event.getFrom().toVector().setY(0).distance(event.getTo().toVector().setY(0));
        double maxSpeed = player.isSprinting() ? 0.288 : 0.222; // Base speeds for sprinting and walking

        // Potion effects
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int speedAmplifier = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier();
            maxSpeed *= (1 + 0.2 * (speedAmplifier + 1));
        }
        if (player.hasPotionEffect(PotionEffectType.JUMP)) {
             maxSpeed *= 1.2; // A simple multiplier for air speed with jump boost
        }

        // Block effects
        Material blockBelow = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        if (blockBelow == Material.ICE || blockBelow == Material.PACKED_ICE || blockBelow == Material.BLUE_ICE) {
            maxSpeed *= 1.5;
        }

        // Add a small buffer to prevent false positives from network lag
        maxSpeed += 0.05;

        if (speed > maxSpeed) {
            flag(playerData);
        }
    }
}
