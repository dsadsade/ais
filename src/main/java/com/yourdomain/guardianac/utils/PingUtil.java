package com.yourdomain.guardianac.utils;

import org.bukkit.entity.Player;

/**
 * Latency compensation utilities for high-ping-friendly detection.
 * All methods are designed to widen thresholds for laggy players
 * while remaining strict for low-ping players.
 */
public final class PingUtil {

    private PingUtil() {}

    /**
     * Get the player's ping in milliseconds, clamped to a sane range.
     */
    public static int getPing(Player player) {
        return Math.max(0, Math.min(player.getPing(), 1000));
    }

    /**
     * Whether the player is considered "high ping" (>150ms).
     */
    public static boolean isHighPing(Player player) {
        return getPing(player) > 150;
    }

    /**
     * Get a reach distance buffer that scales with latency.
     * Low ping (0-50ms): +0.03 blocks
     * Medium ping (50-150ms): +0.08 blocks
     * High ping (150-300ms): +0.15 blocks
     * Very high ping (300ms+): +0.30 blocks, capped at 0.45
     */
    public static double getReachBuffer(Player player) {
        int ping = getPing(player);
        return Math.min(0.45, 0.03 + (ping / 1000.0) * 1.0);
    }

    /**
     * Adjust a time window (in ms) based on ping.
     * For high-ping players, time windows need to be wider to account for
     * packets arriving in bursts.
     */
    public static long adjustTimeWindow(Player player, long baseWindowMs) {
        int ping = getPing(player);
        // Add up to 1.5x the ping as extra window, capped
        long extra = (long)(ping * 1.5);
        return baseWindowMs + Math.min(extra, 600);
    }

    /**
     * Get a tick-based delay compensation. Returns extra ticks to wait
     * before checking results, based on the player's RTT.
     */
    public static int getCompensationTicks(Player player) {
        int ping = getPing(player);
        // 1 tick = 50ms; add ceil(ping / 50) extra ticks, min 1
        return Math.max(1, (int) Math.ceil(ping / 50.0));
    }

    /**
     * Get a rotation threshold multiplier. High-ping players may have
     * interpolation artifacts that look like snaps.
     */
    public static double getRotationMultiplier(Player player) {
        int ping = getPing(player);
        if (ping < 50) return 1.0;
        if (ping < 150) return 1.2;
        if (ping < 300) return 1.5;
        return 1.8;
    }

    /**
     * Get a buffer threshold multiplier. High-ping players need more
     * buffer before flagging to absorb network jitter.
     */
    public static double getBufferMultiplier(Player player) {
        int ping = getPing(player);
        if (ping < 80) return 1.0;
        if (ping < 200) return 1.3;
        if (ping < 400) return 1.6;
        return 2.0;
    }
}
