package com.yourdomain.guardianac.checks.combat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

/**
 * Detects AntiKB (Anti-Knockback) — completely canceling all knockback.
 * Velocity checks (A-C) detect partial KB reduction. This check specifically
 * targets clients that cancel 100% of knockback by checking if the player
 * moved at all after receiving velocity. If they took damage but had zero
 * displacement, it's a hard flag.
 */
public class AntiKBCheck extends Check {

    private static final long KB_CHECK_WINDOW_MS = 500; // check within 500ms of KB
    private static final double MIN_EXPECTED_MOVEMENT = 0.05; // blocks, very lenient

    public AntiKBCheck(GuardianAC plugin) {
        super(plugin, "AntiKB", CheckType.COMBAT);
    }

    @EventHandler
    public void onVelocity(PlayerVelocityEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        Vector v = event.getVelocity();
        double horizontal = Math.sqrt(v.getX() * v.getX() + v.getZ() * v.getZ());

        if (horizontal > 0.1) { // non-trivial knockback
            data.setLastKBReceiveTime(System.currentTimeMillis());
            data.setKbPending(true);
            data.setKbExpectedHorizontal(horizontal);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null || !data.isKbPending()) return;

        long now = System.currentTimeMillis();
        long timeSinceKB = now - data.getLastKBReceiveTime();

        if (timeSinceKB > KB_CHECK_WINDOW_MS) {
            // Window expired — check if they moved
            data.setKbPending(false);
            return;
        }

        // Only check in the sweet spot — 100-500ms after KB
        if (timeSinceKB < 100) return;

        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();
        double movement = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // If they should have been knocked back significantly but didn't move
        if (movement < MIN_EXPECTED_MOVEMENT && data.getKbExpectedHorizontal() > 0.3) {
            handleViolation(data, player, 6.0, 8.0, 0.5);
            data.setKbPending(false);
        } else if (movement > MIN_EXPECTED_MOVEMENT) {
            // They moved — clear the pending check
            data.setKbPending(false);
        }
    }
}
