package com.yourdomain.guardianac.checks.player.badpackets;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * BadPackets (A) — Impossible Pitch Detection
 * Detects pitch values outside the valid range (-90 to 90 degrees).
 * Some hacked clients send impossible pitch values to exploit server-side mechanics.
 */
public class BadPacketsA extends Check {

    private static final float MAX_PITCH = 90.5f;   // tiny tolerance for float precision
    private static final float MIN_PITCH = -90.5f;

    public BadPacketsA(GuardianAC plugin) {
        super(plugin, "BadPackets", "A", CheckType.PLAYER);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        float pitch = event.getTo().getPitch();

        if (pitch > MAX_PITCH || pitch < MIN_PITCH) {
            handleViolation(data, player, 10.0, 5.0, 0.0);
            // Cancel the move — impossible pitch
            event.setCancelled(true);
        }

        data.setLastReportedPitch(pitch);
    }
}
