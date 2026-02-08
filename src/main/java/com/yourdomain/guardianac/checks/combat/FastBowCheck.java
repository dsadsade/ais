package com.yourdomain.guardianac.checks.combat;

import com.yourdomain.guardianac.GuardianAC;
import com.yourdomain.guardianac.checks.Check;
import com.yourdomain.guardianac.checks.CheckType;
import com.yourdomain.guardianac.player.PlayerData;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Material;

/**
 * Detects FastBow hacks — shooting arrows faster than physically possible.
 * A fully charged bow takes 1 second (20 ticks). Fast bow hacks allow instant
 * or very quick bow shots with full force.
 */
public class FastBowCheck extends Check {

    private static final long MIN_DRAW_TIME_MS = 250; // absolute minimum (partially charged)
    private static final float FULL_FORCE_THRESHOLD = 0.9f;
    private static final long FULL_CHARGE_TIME_MS = 900; // ~1 second for full charge

    public FastBowCheck(GuardianAC plugin) {
        super(plugin, "FastBow", CheckType.COMBAT);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() == Material.BOW
                || player.getInventory().getItemInMainHand().getType() == Material.CROSSBOW) {
            PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
            if (data == null) return;

            if (event.getAction().name().contains("RIGHT")) {
                data.setBowDrawStartTime(System.currentTimeMillis());
            }
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!isEnabled()) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (isExempt(player)) return;

        PlayerData data = getPlugin().getPlayerDataManager().getPlayerData(player);
        if (data == null) return;

        long drawStart = data.getBowDrawStartTime();
        if (drawStart <= 0) return;

        long drawTime = System.currentTimeMillis() - drawStart;
        float force = event.getForce();

        // Full force shot with very short draw time
        if (force >= FULL_FORCE_THRESHOLD && drawTime < FULL_CHARGE_TIME_MS) {
            double diff = FULL_CHARGE_TIME_MS - drawTime;
            handleViolation(data, player, diff / 100.0, 10.0, 1.5);
        }

        // Any shot with impossibly short draw time
        if (drawTime < MIN_DRAW_TIME_MS && force > 0.3f) {
            handleViolation(data, player, 6.0, 8.0, 0.5);
        }

        data.setBowDrawStartTime(0);
    }
}
