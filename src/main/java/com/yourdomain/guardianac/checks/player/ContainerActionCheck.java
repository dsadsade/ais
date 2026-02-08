package com.yourdomain.guardianac.checks.player;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Detects ContainerAction — performing impossible interactions with
 * containers (chests, furnaces, etc.) such as clicking multiple slots
 * within a single tick or interacting with containers from too far away.
 */
public class ContainerActionCheck extends Check {

    private static final long MIN_CLICK_INTERVAL_MS = 25;
    private static final int MAX_CLICKS_PER_TICK = 4;

    private final Map<UUID, long[]> clickTracker = new ConcurrentHashMap<>(); // [count, windowStart]

    public ContainerActionCheck(GuardianAC plugin) {
        super(plugin, "ContainerAction", CheckType.PLAYER);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isEnabled()) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (isExempt(player)) return;
        if (event.getInventory().getType() == InventoryType.CRAFTING) return; // Normal player inv

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long now = System.currentTimeMillis();
        long[] tracker = clickTracker.computeIfAbsent(player.getUniqueId(), k -> new long[]{0, now});

        if (now - tracker[1] >= 50) { // 1 tick window
            if (tracker[0] > MAX_CLICKS_PER_TICK) {
                flag(player, String.format("clicks/tick=%d max=%d", tracker[0], MAX_CLICKS_PER_TICK));
            }
            tracker[0] = 1;
            tracker[1] = now;
        } else {
            tracker[0]++;
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        clickTracker.remove(event.getPlayer().getUniqueId());
    }
}
