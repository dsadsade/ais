package com.yourdomain.guardianac.checks.player.animation;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAnimationEvent;

public class AnimationB extends Check {

    public AnimationB(GuardianAC plugin) {
        super(plugin, "Animation", "B", CheckType.PLAYER);
    }

    @EventHandler
    public void onAnimation(PlayerAnimationEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        // Impossible animation state combination — swinging while using item
        if (data.isUsingItem() && data.isBlocking()) {
            handleViolation(data, player, 6, 5, 0.99);
        }

        data.setLastAnimationTime(System.currentTimeMillis());
    }
}
