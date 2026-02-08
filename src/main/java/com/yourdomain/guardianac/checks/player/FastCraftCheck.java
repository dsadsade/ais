package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Detects FastCraft — crafting items impossibly fast, faster than
 * the inventory and crafting table GUI allows.
 */
public class FastCraftCheck extends Check {

    private static final long MIN_CRAFT_INTERVAL_MS = 80;
    private final Map<UUID, Long> lastCraft = new ConcurrentHashMap<>();

    public FastCraftCheck(GuardianAC plugin) {
        super(plugin, "FastCraft", CheckType.PLAYER);
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        Long last = lastCraft.get(player.getUniqueId());

        if (last != null) {
            long delta = now - last;
            if (delta < MIN_CRAFT_INTERVAL_MS && delta >= 0) {
                flag(player, String.format("interval=%dms min=%dms", delta, MIN_CRAFT_INTERVAL_MS));
            }
        }

        lastCraft.put(player.getUniqueId(), now);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastCraft.remove(event.getPlayer().getUniqueId());
    }
}
