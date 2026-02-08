package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffectType;

/**
 * Detects NoWeb hacks — moving through cobwebs at normal or near-normal speed.
 * Cobwebs reduce player movement speed to ~15% of normal walking.
 * NoWeb hacks bypass this slowdown, allowing full speed inside webs.
 */
public class NoWebCheck extends Check {

    private static final double MAX_WEB_SPEED = 0.08; // max horizontal speed in web per tick

    public NoWebCheck(GuardianAC plugin) {
        super(plugin, "NoWeb", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isInsideVehicle()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;
        if (isExempt(player)) return;

        // Check if player is inside a cobweb
        Block playerBlock = player.getLocation().getBlock();
        Block eyeBlock = player.getEyeLocation().getBlock();

        boolean inWeb = playerBlock.getType() == Material.COBWEB || eyeBlock.getType() == Material.COBWEB;
        if (!inWeb) return;

        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();
        double horizontal = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double maxSpeed = MAX_WEB_SPEED;

        // Speed effect gives slight boost even in webs
        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
            int amp = player.getPotionEffect(PotionEffectType.SPEED).getAmplifier();
            maxSpeed *= (1.0 + 0.15 * (amp + 1));
        }

        if (horizontal > maxSpeed) {
            double severity = (horizontal / maxSpeed) * 3.0;
            handleViolation(data, player, severity, 8.0, 1.0);
        }
    }
}
