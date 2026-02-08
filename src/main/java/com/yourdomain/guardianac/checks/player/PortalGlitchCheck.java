package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Detects PortalGlitch — exploiting nether portal mechanics to
 * duplicate items, escape combat, or cause server-side position desync.
 */
public class PortalGlitchCheck extends Check {

    private static final long MIN_PORTAL_INTERVAL_MS = 3000;
    private final Map<UUID, Long> lastPortalUse = new ConcurrentHashMap<>();

    public PortalGlitchCheck(GuardianAC plugin) {
        super(plugin, "PortalGlitch", CheckType.PLAYER);
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        if (!isEnabled()) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        Long last = lastPortalUse.get(player.getUniqueId());

        if (last != null) {
            long delta = now - last;
            if (delta < MIN_PORTAL_INTERVAL_MS && delta >= 0) {
                flag(player, String.format("rapid portal delta=%dms", delta));
                event.setCancelled(true);
            }
        }

        lastPortalUse.put(player.getUniqueId(), now);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        if (!isEnabled()) return;
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL
                && event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) return;

        Player player = event.getPlayer();
        if (isExempt(player)) return;

        long now = System.currentTimeMillis();
        Long last = lastPortalUse.get(player.getUniqueId());

        if (last != null && (now - last) < MIN_PORTAL_INTERVAL_MS) {
            flag(player, String.format("portal teleport spam delta=%dms", now - last));
            event.setCancelled(true);
        }

        lastPortalUse.put(player.getUniqueId(), now);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastPortalUse.remove(event.getPlayer().getUniqueId());
    }
}
