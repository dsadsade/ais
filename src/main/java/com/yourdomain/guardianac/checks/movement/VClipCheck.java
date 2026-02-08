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

/**
 * Detects VClip hacks — vertically teleporting through solid blocks.
 * Players clip through floors/ceilings instantly when using this hack.
 * Normal movement cannot pass through solid blocks vertically.
 */
public class VClipCheck extends Check {

    private static final double MIN_CLIP_DISTANCE = 1.0; // minimum Y change to consider
    private static final double MAX_NORMAL_Y_CHANGE = 0.8; // max normal Y change per tick (jumping)

    public VClipCheck(GuardianAC plugin) {
        super(plugin, "VClip", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isInsideVehicle() || player.isGliding()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;
        if (isExempt(player)) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        double deltaY = to.getY() - from.getY();
        double absDeltaY = Math.abs(deltaY);

        // Only check significant vertical movement
        if (absDeltaY < MIN_CLIP_DISTANCE) {
            data.setPreviousY(to.getY());
            return;
        }

        // Check if there are solid blocks between from and to Y positions
        if (absDeltaY > MAX_NORMAL_Y_CHANGE) {
            int startY = (int) Math.min(from.getY(), to.getY());
            int endY = (int) Math.max(from.getY(), to.getY());

            boolean solidBetween = false;
            for (int y = startY; y <= endY; y++) {
                Block block = new Location(from.getWorld(), from.getX(), y, from.getZ()).getBlock();
                if (block.getType().isSolid() && block.getType() != Material.AIR) {
                    solidBetween = true;
                    break;
                }
            }

            if (solidBetween) {
                handleViolation(data, player, 8.0, 8.0, 0.5);
                event.setCancelled(true);
            }
        }

        data.setPreviousY(to.getY());
    }
}
