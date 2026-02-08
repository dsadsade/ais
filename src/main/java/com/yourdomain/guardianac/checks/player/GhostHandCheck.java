package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.BlockIterator;

/**
 * Detects GhostHand hacks — interacting with blocks through solid walls.
 * Players should only be able to interact with blocks they have line-of-sight to.
 * GhostHand mods allow opening chests, pressing buttons, etc. through solid blocks.
 */
public class GhostHandCheck extends Check {

    private static final double MAX_INTERACT_DISTANCE = 5.0;

    public GhostHandCheck(GuardianAC plugin) {
        super(plugin, "GhostHand", CheckType.PLAYER);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isEnabled()) return;
        if (event.getClickedBlock() == null) return;
        if (!event.getAction().name().contains("RIGHT")) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Block clicked = event.getClickedBlock();

        // Skip non-interactable blocks
        if (!isInteractable(clicked)) return;

        Location eye = player.getEyeLocation();
        double distance = eye.distance(clicked.getLocation().add(0.5, 0.5, 0.5));

        if (distance > MAX_INTERACT_DISTANCE) {
            handleViolation(data, player, 5.0, 8.0, 0.5);
            event.setCancelled(true);
            return;
        }

        // Ray trace from eye to clicked block; check for solid blocks in between
        try {
            BlockIterator iter = new BlockIterator(eye.getWorld(),
                    eye.toVector(),
                    eye.getDirection(),
                    0, (int) Math.ceil(distance) + 1);

            while (iter.hasNext()) {
                Block next = iter.next();

                // Reached the target block — all clear
                if (next.equals(clicked)) break;

                // Found a solid block between player and target
                if (next.getType().isSolid() && next.getType().isOccluding()) {
                    handleViolation(data, player, 6.0, 8.0, 0.5);
                    event.setCancelled(true);
                    return;
                }
            }
        } catch (IllegalStateException ignored) {
            // BlockIterator can throw if direction is zero; ignore
        }
    }

    private boolean isInteractable(Block block) {
        String name = block.getType().name();
        return name.contains("CHEST") || name.contains("BARREL") || name.contains("SHULKER")
                || name.contains("FURNACE") || name.contains("HOPPER") || name.contains("DISPENSER")
                || name.contains("DROPPER") || name.contains("BREWING") || name.contains("ANVIL")
                || name.contains("ENCHANTING") || name.contains("BEACON")
                || name.contains("BUTTON") || name.contains("LEVER") || name.contains("DOOR")
                || name.contains("GATE") || name.contains("TRAPDOOR");
    }
}
