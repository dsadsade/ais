package com.yourdomain.guardianac.checks.movement;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

public class FlyCheck extends Check {

    public FlyCheck(GuardianAC plugin) {
        super(plugin, "Fly", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        PlayerData playerData = getPlugin().getPlayerDataManager().getPlayerData(player);

        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR || player.isFlying() || player.getAllowFlight() || player.hasPotionEffect(PotionEffectType.LEVITATION)) {
            return;
        }

        // Improved check to avoid flagging normal falls
        if (!player.isOnGround() && event.getTo().getY() > event.getFrom().getY()) {
            // Player is moving up without jumping
            if (player.getVelocity().getY() > 0 && !hasSolidBlockBelow(player.getLocation())) {
                 // Further checks can be added here, e.g., for bounce pads, etc.
                 flag(playerData);
            }
        }
    }

    private boolean hasSolidBlockBelow(Location location) {
        // Check for solid blocks in a 3x3 area below the player
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                for (int y = -1; y >= -3; y--) { // Check 3 blocks down
                    if (location.clone().add(x, y, z).getBlock().getType().isSolid()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
