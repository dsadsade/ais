package com.yourdomain.guardianac.checks.player.actionspoof;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerAnimationEvent;

public class ActionSpoofC extends Check {

    public ActionSpoofC(GuardianAC plugin) {
        super(plugin, "ActionSpoof", "C", CheckType.PLAYER);
    }

    @EventHandler
    public void onAnimation(PlayerAnimationEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long lastAnim = data.getLastAnimationTime();

        // Fake animation state — impossible rapid animation toggling
        if (lastAnim > 0 && now - lastAnim < 5) {
            handleViolation(data, player, 4, 10, 0.995);
        }

        data.setLastAnimationTime(now);
    }
}
