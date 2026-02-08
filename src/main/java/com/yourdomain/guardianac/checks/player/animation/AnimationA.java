package com.yourdomain.guardianac.checks.player.animation;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

public class AnimationA extends Check {

    public AnimationA(GuardianAC plugin) {
        super(plugin, "Animation", "A", CheckType.PLAYER);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long lastSwing = data.getLastAnimationTime();

        // Missing arm swing before action — no animation packet before block break
        if (lastSwing <= 0 || now - lastSwing > 200) {
            handleViolation(data, player, 5, 8, 0.99);
        }
    }
}
