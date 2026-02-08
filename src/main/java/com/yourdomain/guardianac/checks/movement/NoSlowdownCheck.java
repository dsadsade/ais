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

/**
 * Detects NoSlowdown — moving at full speed through slowing blocks
 * (cobwebs, soul sand, honey blocks, sweet berry bushes).
 */
public class NoSlowdownCheck extends Check {

    private static final double COBWEB_MAX_SPEED = 0.07;
    private static final double SOUL_SAND_MAX_SPEED = 0.22;
    private static final double HONEY_MAX_SPEED = 0.15;
    private static final double BERRY_MAX_SPEED = 0.12;

    public NoSlowdownCheck(GuardianAC plugin) {
        super(plugin, "NoSlowdown", CheckType.MOVEMENT);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
        if (player.isFlying()) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Block block = player.getLocation().getBlock();
        Material mat = block.getType();
        double dx = event.getTo().getX() - event.getFrom().getX();
        double dz = event.getTo().getZ() - event.getFrom().getZ();
        double horizontalSpeed = Math.sqrt(dx * dx + dz * dz);

        double maxAllowed = -1;
        if (mat == Material.COBWEB) {
            maxAllowed = COBWEB_MAX_SPEED;
        } else if (mat == Material.SOUL_SAND) {
            maxAllowed = SOUL_SAND_MAX_SPEED;
        } else if (mat == Material.HONEY_BLOCK) {
            maxAllowed = HONEY_MAX_SPEED;
        } else if (mat == Material.SWEET_BERRY_BUSH) {
            maxAllowed = BERRY_MAX_SPEED;
        }

        if (maxAllowed > 0 && horizontalSpeed > maxAllowed) {
            flag(player, String.format("speed=%.4f max=%.4f block=%s", horizontalSpeed, maxAllowed, mat.name()));
        }
    }
}
