package com.yourdomain.guardianac.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores per-player tracking data for all anticheat checks.
 * Thread-safe maps are used where async access is possible (violation decay task).
 */
public class PlayerData {

    private final UUID playerUUID;

    // ── Violation Management ──
    private final Map<String, Integer> violationLevels = new ConcurrentHashMap<>();
    private final Map<String, Long> violationTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Double> checkBuffers = new ConcurrentHashMap<>();

    // ── Rotation Tracking (per-tick) ──
    private float lastYaw;
    private float lastPitch;
    private float prevLastYaw;
    private float prevLastPitch;
    private final List<Float> yawDeltaHistory = new ArrayList<>();
    private final List<Float> pitchDeltaHistory = new ArrayList<>();
    private final List<float[]> rotationSnapshots = new ArrayList<>();
    private final List<Double> gcdSamples = new ArrayList<>();

    // ── Attack / Hit Tracking ──
    private long lastAttackTime;
    private final List<Long> attackTimestamps = new ArrayList<>();
    private final List<Double> attackIntervals = new ArrayList<>();
    private int lastTargetEntityId = -1;
    private int targetSwitchCount;
    private long targetSwitchWindowStart;
    private int consecutiveHits;
    private int swingCount;
    private int hitCount;
    private long swingWindowStart;
    private Location lastTargetLocation;
    private final List<Double> recentReachDistances = new ArrayList<>();
    private final List<Float> attackYawDeltas = new ArrayList<>();
    private final List<Float> attackPitchDeltas = new ArrayList<>();

    // ── Pre/Post Attack Rotation ──
    private float preAttackYaw;
    private float preAttackPitch;
    private float postAttackYaw;
    private float postAttackPitch;
    private boolean attackedThisTick;

    // ── Sprint / Block Tracking ──
    private boolean wasSprinting;
    private long lastSprintToggleTime;
    private int sprintToggleCount;
    private long sprintToggleWindowStart;
    private boolean wasBlocking;
    private long lastBlockTime;
    private long lastBlockEndTime;

    // ── Click Tracking ──
    private int clickCount;
    private long clickWindowStart;
    private final List<Long> clickTimestamps = new ArrayList<>();
    private final List<Long> clickIntervals = new ArrayList<>();

    // ── Velocity Tracking ──
    private double lastVelocityX;
    private double lastVelocityY;
    private double lastVelocityZ;
    private long lastDamageTime;
    private boolean pendingVelocityCheck;

    // ── Movement Tracking ──
    private Location lastGroundLocation;
    private long lastOnGroundTime;
    private int airTicks;
    private double lastDeltaY;
    private final List<Double> recentSpeeds = new ArrayList<>();
    private int hoverTicks;
    private int consecutiveSpeedFlags;

    // ── Timing Tracking ──
    private long lastMovePacketTime;
    private int packetCount;
    private long packetWindowStart;

    // ── Scaffold Tracking ──
    private int rapidBlockPlacements;
    private long lastBlockPlaceTime;
    private Location lastBlockPlaceLocation;

    // ── NoSlow Tracking ──
    private boolean isUsingItem;
    private long itemUseStartTime;

    // ── Step / Jump Tracking ──
    private double lowestY;
    private Location jumpStartLocation;
    private long jumpStartTime;
    private int stepFlags;

    // ── Inventory Move Tracking ──
    private boolean inventoryOpen;
    private long inventoryOpenTime;

    // ── Elytra Tracking ──
    private double lastElytraSpeed;
    private int elytraSpeedFlags;

    // ── Blink Tracking ──
    private long lastRealMoveTime;
    private final List<Long> movementGaps = new ArrayList<>();
    private int blinkTicks;

    // ── Swing / Arm Animation Tracking ──
    private long lastSwingTime;
    private boolean pendingSwing;

    // ── Bow Tracking ──
    private long bowDrawStartTime;

    // ── Criticals Tracking ──
    private boolean wasOnGroundBeforeAttack;

    // ── AimAssist Tracking ──
    private final List<Float> smoothYawDeltas = new ArrayList<>();
    private final List<Float> smoothPitchDeltas = new ArrayList<>();
    private int snapCorrectionCount;
    private long snapCorrectionWindowStart;

    // ── FastBreak / FastPlace / Nuker Tracking ──
    private final List<Long> blockBreakTimes = new ArrayList<>();
    private final List<Long> blockPlaceTimes = new ArrayList<>();
    private int nukerBreakCount;
    private long nukerWindowStart;
    private Location lastBreakLocation;

    // ── ChestStealer Tracking ──
    private final List<Long> chestClickTimes = new ArrayList<>();

    // ── Chat / Command / Drop Spam Tracking ──
    private final List<Long> chatTimes = new ArrayList<>();
    private final List<Long> commandTimes = new ArrayList<>();
    private final List<Long> dropTimes = new ArrayList<>();

    // ── Ping Spoof Tracking ──
    private final List<Integer> recentPings = new ArrayList<>();

    // ── BadPackets Tracking ──
    private float lastReportedPitch;
    private boolean lastOnGround;
    private int invalidGroundTicks;

    // ── Strafe Tracking ──
    private double lastDeltaX;
    private double lastDeltaZ;
    private int impossibleStrafeCount;

    // ── VClip Tracking ──
    private double previousY;

    // ── Backtrack Tracking ──
    private final List<Location> recentAttackLocations = new ArrayList<>();
    private long lastAttackLocationTime;

    // ── FastProjectile Tracking ──
    private final List<Long> projectileThrowTimes = new ArrayList<>();

    // ── MultiAura Tracking ──
    private final Set<Integer> attackedEntitiesThisTick = new HashSet<>();
    private long lastAttackTickTime;

    // ── AutoArmor / AutoTotem Tracking ──
    private final List<Long> armorEquipTimes = new ArrayList<>();
    private final List<Long> totemSwapTimes = new ArrayList<>();

    // ── Regen Tracking ──
    private final List<Double> healthHistory = new ArrayList<>();
    private long lastHealthCheckTime;

    // ── AntiHunger Tracking ──
    private final List<Integer> hungerHistory = new ArrayList<>();
    private long lastHungerCheckTime;
    private int staleHungerTicks;

    // ── AutoTool Tracking ──
    private final List<Long> toolSwitchTimes = new ArrayList<>();

    // ── Xray Tracking ──
    private final Map<String, Integer> oreMinedCounts = new HashMap<>();
    private int totalBlocksMined;

    // ── Tower Tracking ──
    private int towerPlaceCount;
    private double towerStartY;
    private long towerWindowStart;

    // ── AntiLevitation Tracking ──
    private int levitationIgnoreTicks;

    // ── PacketFlood Tracking ──
    private int totalPacketsPerSecond;
    private long packetFloodWindowStart;

    // ── FreeCam Tracking ──
    private int noMovementWithRotationTicks;

    // ── TargetStrafe Tracking ──
    private final List<Double> targetStrafeAngles = new ArrayList<>();
    private double lastTargetStrafeAngle;

    // ── WTap Tracking ──
    private int wTapCount;
    private long wTapWindowStart;

    // ── NoSlowBow Tracking ──
    private boolean bowDrawing;

    // ── AutoSwitch Tracking ──
    private int lastHeldSlot;
    private long lastSlotSwitchTime;
    private int slotSwitchBeforeAttackCount;

    // ── TPAura Tracking ──
    private Location lastPositionBeforeAttack;
    private int tpAuraFlags;

    // ── AntiKB Tracking ──
    private long lastKBReceiveTime;
    private boolean kbPending;
    private double kbExpectedHorizontal;

    // ── ClickAura Tracking ──
    private int behindBackHitCount;
    private long clickAuraWindowStart;

    // ── Reach D Tracking ──
    private final List<Double> verticalReachDistances = new ArrayList<>();

    // ── Aimbot Tracking ──
    private final List<Float> aimLockSamples = new ArrayList<>();
    private int perfectAimCount;
    private final List<Double> aimPredictionDeltas = new ArrayList<>();

    // ── AirJump Tracking ──
    private int airJumpCount;
    private double lastJumpY;

    // ── Spider Tracking ──
    private int wallClimbTicks;

    // ── Glide Tracking ──
    private int glideTicks;
    private double lastFallRate;

    // ── FastClimb Tracking ──
    private double climbSpeed;
    private int climbSpeedFlags;

    // ── BunnyHop Tracking ──
    private final List<Double> bhopSpeedHistory = new ArrayList<>();
    private int bhopJumpCount;

    // ── NoRotate / AntiAim Tracking ──
    private double lastMovementDirection;
    private int noRotateFlags;

    // ── Derp Tracking ──
    private int rotationChangeCount;
    private long rotationChangeWindowStart;

    // ── AutoFish Tracking ──
    private final List<Long> fishCastTimes = new ArrayList<>();
    private final List<Long> fishReelTimes = new ArrayList<>();

    // ── InventoryClick Tracking ──
    private final List<Long> inventoryClickTimes = new ArrayList<>();

    // ── SkinBlinker Tracking ──
    private int skinChangeCount;
    private long skinChangeWindowStart;

    // ── AntiAFK Tracking ──
    private long lastActualInputTime;
    private int afkPatternCount;
    private final List<Double> afkMovementAngles = new ArrayList<>();

    // ── FastHeal Tracking ──
    private final List<Long> healEventTimes = new ArrayList<>();

    // ── BookExploit Tracking ──
    private int bookEditCount;
    private long bookEditWindowStart;

    // ── Baritone Tracking ──
    private int straightLineCount;
    private int pathPatternCount;
    private Location lastPathPoint;

    // ── SelfDamage Tracking ──
    private int selfDamageCount;
    private long selfDamageWindowStart;

    // ── AutoBlock Tracking ──
    private int shieldToggleCount;
    private long lastShieldToggleTime;
    private long shieldToggleWindowStart;

    // ── TriggerBot Tracking ──
    private int crosshairAttackCount;
    private long crosshairTrackWindowStart;

    // ── NoHitDelay Tracking ──
    private long lastEntityHurtTime;
    private int consecutiveImmediateHits;

    // ── Extended Click Analysis ──
    private final List<Long> extendedClickIntervals = new ArrayList<>();

    // ── FightBot Tracking ──
    private int combatActionScore;
    private long combatActionWindowStart;

    // ── AttackAura Tracking ──
    private final List<Integer> targetCycleOrder = new ArrayList<>();

    // ── Flight Extended Tracking ──
    private int packetFlightTicks;
    private double lastVerticalAcceleration;
    private int creativeFlightTicks;

    // ── GroundSpoof Tracking ──
    private int groundSpoofFlags;

    // ── Teleport Tracking ──
    private final List<Double> positionJumps = new ArrayList<>();

    // ── SafeWalk Tracking ──
    private int edgeWalkCount;
    private long edgeWalkWindowStart;

    // ── FastSneak Tracking ──
    private double lastSneakSpeed;
    private int fastSneakFlags;

    // ── FastSwim Tracking ──
    private double lastSwimSpeed;
    private int fastSwimFlags;

    // ── BoatSpeed Tracking ──
    private double lastBoatSpeed;
    private int boatSpeedFlags;

    // ── HorseJump Tracking ──
    private double lastMountJumpHeight;
    private int mountSpeedFlags;

    // ── WaterWalk Tracking ──
    private int waterSurfaceTicks;

    // ── AntiSlip Tracking ──
    private int iceMovementFlags;

    // ── AutoJump Tracking ──
    private int autoJumpTicks;
    private long autoJumpWindowStart;

    // ── PhaseWalk Tracking ──
    private int horizontalPhaseFlags;

    // ── GravityOverride Tracking ──
    private int gravityViolationTicks;

    // ── MovementPrediction Tracking ──
    private final List<Double> movementPredictionDeltas = new ArrayList<>();

    // ── Protocol Tracking ──
    private long lastPacketSequenceTime;
    private int invalidPacketSequenceCount;

    // ── Inventory Extended Tracking ──
    private int inventoryManipulationFlags;
    private long lastInventoryAction;

    // ── PacketSpam Extended Tracking ──
    private int actionPacketsPerSecond;
    private int blockPacketsPerSecond;

    // ── Interact Tracking ──
    private int invalidInteractCount;

    // ── ActionSpoof Tracking ──
    private int actionSpoofCount;

    // ── NoRotation Tracking ──
    private int headFreezeCount;

    // ── AutoEat Tracking ──
    private final List<Long> eatStartTimes = new ArrayList<>();

    // ── HealthSpoof Tracking ──
    private double lastServerHealth;

    // ── ImpossibleSlot Tracking ──
    private int impossibleSlotAccessCount;

    // ── EntityAction Tracking ──
    private int entityActionSpoofCount;

    // ── Extended Tracking Fields (missing in original) ──
    private long lastPacketTime;
    private long lastDigStartTime;
    private long eatStartTime;
    private String clientBrand = "vanilla";
    private long lastAnimationTime;
    private long lastTickTime;
    private long lastInteractTime;
    private double expectedHealth;
    private long lastEntityActionTime;
    private boolean inBed;
    private int rotationMismatchStreak;
    private int rotationFrozenTicks;
    private boolean blocking;
    private long lastShiftClickTime;
    private double lastRecordedHealth;
    private boolean sneaking;
    private boolean sprinting;
    private long lastSlotChangeTime;
    private boolean horseJumping;
    private int movePacketCount;
    private final List<Long> timingDeltas = new ArrayList<>();
    private int consecutiveEatCount;
    private long lastEatTime;
    private long lastMoveTime;
    private long lastTeleportTime;
    private long lastInventoryClickTime;
    private long lastInventoryCloseTime;

    // ── ServerCrasher Tracking ──
    private int entitySpawnCount;
    private long entitySpawnWindowStart;
    private int signEditCount;
    private long signEditWindowStart;

    // ── DupeDetect Tracking ──
    private int suspiciousDropCount;
    private long suspiciousDropWindowStart;

    // ── NBTExploit Tracking ──
    private int largeNBTCount;

    // ── EntityExploit Tracking ──
    private int vehicleStackCount;

    // ── ChunkExploit Tracking ──
    private int chunkRequestCount;
    private long chunkRequestWindowStart;

    // ── PayloadExploit Tracking ──
    private int customPayloadCount;
    private long customPayloadWindowStart;

    // ── Connection Tracking ──
    private int reconnectCount;
    private long lastDisconnectTime;

    // ── ProtocolExploit Tracking ──
    private int invalidProtocolCount;

    // ── ItemExploit Tracking ──
    private int invalidItemActionCount;

    // ── ReducedKB Tracking ──
    private double kbActualHorizontal;
    private int partialKBCount;

    // ── HitModifier Tracking ──
    private double lastDamageDealt;
    private int damageModifierFlags;

    // ── SprintReset Extended ──
    private int perfectSprintResets;
    private long sprintResetWindowStart;

    // ── DamageIndicator Tracking ──
    private int damageIndicatorFlags;

    // ── CombatAnalysis Tracking ──
    private int overallCombatScore;
    private long combatSessionStart;

    public PlayerData(Player player) {
        this.playerUUID = player.getUniqueId();
        this.lastOnGroundTime = System.currentTimeMillis();
        this.lastMovePacketTime = System.currentTimeMillis();
        this.packetWindowStart = System.currentTimeMillis();
        this.clickWindowStart = System.currentTimeMillis();
        this.swingWindowStart = System.currentTimeMillis();
        this.sprintToggleWindowStart = System.currentTimeMillis();
        this.targetSwitchWindowStart = System.currentTimeMillis();
        this.lastYaw = player.getLocation().getYaw();
        this.lastPitch = player.getLocation().getPitch();
    }

    // ═══ VIOLATION MANAGEMENT ═══
    public UUID getPlayerUUID() { return playerUUID; }
    public int getViolationLevel(String key) { return violationLevels.getOrDefault(key, 0); }
    public void incrementViolationLevel(String key) {
        violationLevels.put(key, getViolationLevel(key) + 1);
        violationTimestamps.put(key, System.currentTimeMillis());
    }
    public void resetViolationLevel(String key) { violationLevels.remove(key); violationTimestamps.remove(key); }
    public long getLastViolationTime(String key) { return violationTimestamps.getOrDefault(key, 0L); }
    public void decayViolations(long decayAfterMs) {
        long now = System.currentTimeMillis();
        Iterator<Map.Entry<String, Long>> it = violationTimestamps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Long> entry = it.next();
            if (now - entry.getValue() > decayAfterMs) {
                String check = entry.getKey();
                int current = violationLevels.getOrDefault(check, 0);
                if (current > 0) { violationLevels.put(check, current - 1); entry.setValue(now); }
                if (violationLevels.getOrDefault(check, 0) <= 0) { violationLevels.remove(check); it.remove(); }
            }
        }
    }

    // ═══ CHECK BUFFER SYSTEM ═══
    public double getCheckBuffer(String key) { return checkBuffers.getOrDefault(key, 0.0); }
    public void setCheckBuffer(String key, double value) { checkBuffers.put(key, value); }

    // ═══ ROTATION TRACKING ═══
    public float getLastYaw() { return lastYaw; }
    public float getLastPitch() { return lastPitch; }
    public float getPrevLastYaw() { return prevLastYaw; }
    public float getPrevLastPitch() { return prevLastPitch; }
    public void updateRotation(float yaw, float pitch) {
        this.prevLastYaw = this.lastYaw; this.prevLastPitch = this.lastPitch;
        this.lastYaw = yaw; this.lastPitch = pitch;
    }
    public List<Float> getYawDeltaHistory() { return yawDeltaHistory; }
    public List<Float> getPitchDeltaHistory() { return pitchDeltaHistory; }
    public void addYawDelta(float d) { yawDeltaHistory.add(d); if (yawDeltaHistory.size() > 40) yawDeltaHistory.remove(0); }
    public void addPitchDelta(float d) { pitchDeltaHistory.add(d); if (pitchDeltaHistory.size() > 40) pitchDeltaHistory.remove(0); }
    public List<float[]> getRotationSnapshots() { return rotationSnapshots; }
    public void addRotationSnapshot(float y, float p) { rotationSnapshots.add(new float[]{y, p}); if (rotationSnapshots.size() > 40) rotationSnapshots.remove(0); }
    public List<Double> getGcdSamples() { return gcdSamples; }
    public void addGcdSample(double g) { gcdSamples.add(g); if (gcdSamples.size() > 50) gcdSamples.remove(0); }

    // ═══ ATTACK / HIT TRACKING ═══
    public long getLastAttackTime() { return lastAttackTime; }
    public void setLastAttackTime(long t) { this.lastAttackTime = t; }
    public List<Long> getAttackTimestamps() { return attackTimestamps; }
    public void addAttackTimestamp(long t) { attackTimestamps.add(t); if (attackTimestamps.size() > 50) attackTimestamps.remove(0); }
    public List<Double> getAttackIntervals() { return attackIntervals; }
    public void addAttackInterval(double i) { attackIntervals.add(i); if (attackIntervals.size() > 40) attackIntervals.remove(0); }
    public int getLastTargetEntityId() { return lastTargetEntityId; }
    public void setLastTargetEntityId(int id) { this.lastTargetEntityId = id; }
    public int getTargetSwitchCount() { return targetSwitchCount; }
    public void incrementTargetSwitchCount() { this.targetSwitchCount++; }
    public void resetTargetSwitchCount() { this.targetSwitchCount = 0; }
    public long getTargetSwitchWindowStart() { return targetSwitchWindowStart; }
    public void setTargetSwitchWindowStart(long t) { this.targetSwitchWindowStart = t; }
    public int getConsecutiveHits() { return consecutiveHits; }
    public void incrementConsecutiveHits() { this.consecutiveHits++; }
    public void resetConsecutiveHits() { this.consecutiveHits = 0; }
    public int getSwingCount() { return swingCount; }
    public void incrementSwingCount() { this.swingCount++; }
    public void resetSwingCount() { this.swingCount = 0; }
    public int getHitCount() { return hitCount; }
    public void incrementHitCount() { this.hitCount++; }
    public void resetHitCount() { this.hitCount = 0; }
    public long getSwingWindowStart() { return swingWindowStart; }
    public void setSwingWindowStart(long t) { this.swingWindowStart = t; }
    public Location getLastTargetLocation() { return lastTargetLocation; }
    public void setLastTargetLocation(Location l) { this.lastTargetLocation = l; }
    public List<Double> getRecentReachDistances() { return recentReachDistances; }
    public void addReachDistance(double d) { recentReachDistances.add(d); if (recentReachDistances.size() > 30) recentReachDistances.remove(0); }
    public List<Float> getAttackYawDeltas() { return attackYawDeltas; }
    public void addAttackYawDelta(float d) { attackYawDeltas.add(d); if (attackYawDeltas.size() > 30) attackYawDeltas.remove(0); }
    public List<Float> getAttackPitchDeltas() { return attackPitchDeltas; }
    public void addAttackPitchDelta(float d) { attackPitchDeltas.add(d); if (attackPitchDeltas.size() > 30) attackPitchDeltas.remove(0); }

    // ── Pre/Post attack rotation ──
    public float getPreAttackYaw() { return preAttackYaw; }
    public float getPreAttackPitch() { return preAttackPitch; }
    public void setPreAttackRotation(float y, float p) { this.preAttackYaw = y; this.preAttackPitch = p; }
    public float getPostAttackYaw() { return postAttackYaw; }
    public float getPostAttackPitch() { return postAttackPitch; }
    public void setPostAttackRotation(float y, float p) { this.postAttackYaw = y; this.postAttackPitch = p; }
    public boolean hasAttackedThisTick() { return attackedThisTick; }
    public void setAttackedThisTick(boolean v) { this.attackedThisTick = v; }

    // ═══ SPRINT / BLOCK TRACKING ═══
    public boolean wasSprinting() { return wasSprinting; }
    public void setWasSprinting(boolean v) { this.wasSprinting = v; }
    public long getLastSprintToggleTime() { return lastSprintToggleTime; }
    public void setLastSprintToggleTime(long t) { this.lastSprintToggleTime = t; }
    public int getSprintToggleCount() { return sprintToggleCount; }
    public void incrementSprintToggleCount() { this.sprintToggleCount++; }
    public void resetSprintToggleCount() { this.sprintToggleCount = 0; }
    public long getSprintToggleWindowStart() { return sprintToggleWindowStart; }
    public void setSprintToggleWindowStart(long t) { this.sprintToggleWindowStart = t; }
    public boolean wasBlocking() { return wasBlocking; }
    public void setWasBlocking(boolean v) { this.wasBlocking = v; }
    public long getLastBlockTime() { return lastBlockTime; }
    public void setLastBlockTime(long t) { this.lastBlockTime = t; }
    public long getLastBlockEndTime() { return lastBlockEndTime; }
    public void setLastBlockEndTime(long t) { this.lastBlockEndTime = t; }

    // ═══ CLICK TRACKING ═══
    public int getClickCount() { return clickCount; }
    public void incrementClickCount() { this.clickCount++; }
    public void resetClickCount() { this.clickCount = 0; }
    public long getClickWindowStart() { return clickWindowStart; }
    public void setClickWindowStart(long t) { this.clickWindowStart = t; }
    public List<Long> getClickTimestamps() { return clickTimestamps; }
    public void addClickTimestamp(long t) { clickTimestamps.add(t); if (clickTimestamps.size() > 100) clickTimestamps.remove(0); }
    public List<Long> getClickIntervals() { return clickIntervals; }
    public void addClickInterval(long i) { clickIntervals.add(i); if (clickIntervals.size() > 80) clickIntervals.remove(0); }

    // ═══ VELOCITY TRACKING ═══
    public double getLastVelocityX() { return lastVelocityX; }
    public double getLastVelocityY() { return lastVelocityY; }
    public double getLastVelocityZ() { return lastVelocityZ; }
    public void setLastVelocity(double x, double y, double z) { this.lastVelocityX = x; this.lastVelocityY = y; this.lastVelocityZ = z; }
    public long getLastDamageTime() { return lastDamageTime; }
    public void setLastDamageTime(long t) { this.lastDamageTime = t; }
    public boolean isPendingVelocityCheck() { return pendingVelocityCheck; }
    public void setPendingVelocityCheck(boolean v) { this.pendingVelocityCheck = v; }

    // ═══ MOVEMENT TRACKING ═══
    public Location getLastGroundLocation() { return lastGroundLocation; }
    public void setLastGroundLocation(Location l) { this.lastGroundLocation = l; }
    public long getLastOnGroundTime() { return lastOnGroundTime; }
    public void setLastOnGroundTime(long t) { this.lastOnGroundTime = t; }
    public int getAirTicks() { return airTicks; }
    public void setAirTicks(int v) { this.airTicks = v; }
    public void incrementAirTicks() { this.airTicks++; }
    public double getLastDeltaY() { return lastDeltaY; }
    public void setLastDeltaY(double v) { this.lastDeltaY = v; }
    public List<Double> getRecentSpeeds() { return recentSpeeds; }
    public void addSpeed(double s) { recentSpeeds.add(s); if (recentSpeeds.size() > 20) recentSpeeds.remove(0); }
    public int getHoverTicks() { return hoverTicks; }
    public void setHoverTicks(int v) { this.hoverTicks = v; }
    public int getConsecutiveSpeedFlags() { return consecutiveSpeedFlags; }
    public void setConsecutiveSpeedFlags(int v) { this.consecutiveSpeedFlags = v; }

    // ═══ TIMING TRACKING ═══
    public long getLastMovePacketTime() { return lastMovePacketTime; }
    public void setLastMovePacketTime(long t) { this.lastMovePacketTime = t; }
    public int getPacketCount() { return packetCount; }
    public void incrementPacketCount() { this.packetCount++; }
    public void resetPacketCount() { this.packetCount = 0; }
    public long getPacketWindowStart() { return packetWindowStart; }
    public void setPacketWindowStart(long t) { this.packetWindowStart = t; }

    // ═══ SCAFFOLD TRACKING ═══
    public int getRapidBlockPlacements() { return rapidBlockPlacements; }
    public void incrementRapidBlockPlacements() { this.rapidBlockPlacements++; }
    public void resetRapidBlockPlacements() { this.rapidBlockPlacements = 0; }
    public long getLastBlockPlaceTime() { return lastBlockPlaceTime; }
    public void setLastBlockPlaceTime(long t) { this.lastBlockPlaceTime = t; }
    public Location getLastBlockPlaceLocation() { return lastBlockPlaceLocation; }
    public void setLastBlockPlaceLocation(Location l) { this.lastBlockPlaceLocation = l; }

    // ═══ NOSLOW TRACKING ═══
    public boolean isUsingItem() { return isUsingItem; }
    public void setUsingItem(boolean v) { this.isUsingItem = v; }
    public long getItemUseStartTime() { return itemUseStartTime; }
    public void setItemUseStartTime(long t) { this.itemUseStartTime = t; }

    // ═══ STEP / JUMP TRACKING ═══
    public double getLowestY() { return lowestY; }
    public void setLowestY(double v) { this.lowestY = v; }
    public Location getJumpStartLocation() { return jumpStartLocation; }
    public void setJumpStartLocation(Location l) { this.jumpStartLocation = l; }
    public long getJumpStartTime() { return jumpStartTime; }
    public void setJumpStartTime(long t) { this.jumpStartTime = t; }
    public int getStepFlags() { return stepFlags; }
    public void setStepFlags(int v) { this.stepFlags = v; }

    // ═══ INVENTORY MOVE TRACKING ═══
    public boolean isInventoryOpen() { return inventoryOpen; }
    public void setInventoryOpen(boolean v) { this.inventoryOpen = v; }
    public long getInventoryOpenTime() { return inventoryOpenTime; }
    public void setInventoryOpenTime(long t) { this.inventoryOpenTime = t; }

    // ═══ ELYTRA TRACKING ═══
    public double getLastElytraSpeed() { return lastElytraSpeed; }
    public void setLastElytraSpeed(double v) { this.lastElytraSpeed = v; }
    public int getElytraSpeedFlags() { return elytraSpeedFlags; }
    public void setElytraSpeedFlags(int v) { this.elytraSpeedFlags = v; }

    // ═══ BLINK TRACKING ═══
    public long getLastRealMoveTime() { return lastRealMoveTime; }
    public void setLastRealMoveTime(long t) { this.lastRealMoveTime = t; }
    public List<Long> getMovementGaps() { return movementGaps; }
    public void addMovementGap(long g) { movementGaps.add(g); if (movementGaps.size() > 20) movementGaps.remove(0); }
    public int getBlinkTicks() { return blinkTicks; }
    public void setBlinkTicks(int v) { this.blinkTicks = v; }

    // ═══ SWING TRACKING ═══
    public long getLastSwingTime() { return lastSwingTime; }
    public void setLastSwingTime(long t) { this.lastSwingTime = t; }
    public boolean isPendingSwing() { return pendingSwing; }
    public void setPendingSwing(boolean v) { this.pendingSwing = v; }

    // ═══ BOW TRACKING ═══
    public long getBowDrawStartTime() { return bowDrawStartTime; }
    public void setBowDrawStartTime(long t) { this.bowDrawStartTime = t; }

    // ═══ CRITICALS TRACKING ═══
    public boolean wasOnGroundBeforeAttack() { return wasOnGroundBeforeAttack; }
    public void setWasOnGroundBeforeAttack(boolean v) { this.wasOnGroundBeforeAttack = v; }

    // ═══ AIMASSIST TRACKING ═══
    public List<Float> getSmoothYawDeltas() { return smoothYawDeltas; }
    public void addSmoothYawDelta(float d) { smoothYawDeltas.add(d); if (smoothYawDeltas.size() > 40) smoothYawDeltas.remove(0); }
    public List<Float> getSmoothPitchDeltas() { return smoothPitchDeltas; }
    public void addSmoothPitchDelta(float d) { smoothPitchDeltas.add(d); if (smoothPitchDeltas.size() > 40) smoothPitchDeltas.remove(0); }
    public int getSnapCorrectionCount() { return snapCorrectionCount; }
    public void incrementSnapCorrectionCount() { this.snapCorrectionCount++; }
    public void resetSnapCorrectionCount() { this.snapCorrectionCount = 0; }
    public long getSnapCorrectionWindowStart() { return snapCorrectionWindowStart; }
    public void setSnapCorrectionWindowStart(long t) { this.snapCorrectionWindowStart = t; }

    // ═══ FASTBREAK / FASTPLACE / NUKER TRACKING ═══
    public List<Long> getBlockBreakTimes() { return blockBreakTimes; }
    public void addBlockBreakTime(long t) { blockBreakTimes.add(t); if (blockBreakTimes.size() > 30) blockBreakTimes.remove(0); }
    public List<Long> getBlockPlaceTimes() { return blockPlaceTimes; }
    public void addBlockPlaceTime(long t) { blockPlaceTimes.add(t); if (blockPlaceTimes.size() > 30) blockPlaceTimes.remove(0); }
    public int getNukerBreakCount() { return nukerBreakCount; }
    public void incrementNukerBreakCount() { this.nukerBreakCount++; }
    public void resetNukerBreakCount() { this.nukerBreakCount = 0; }
    public long getNukerWindowStart() { return nukerWindowStart; }
    public void setNukerWindowStart(long t) { this.nukerWindowStart = t; }
    public Location getLastBreakLocation() { return lastBreakLocation; }
    public void setLastBreakLocation(Location l) { this.lastBreakLocation = l; }

    // ═══ CHESTSTEALER TRACKING ═══
    public List<Long> getChestClickTimes() { return chestClickTimes; }
    public void addChestClickTime(long t) { chestClickTimes.add(t); if (chestClickTimes.size() > 20) chestClickTimes.remove(0); }

    // ═══ SPAM TRACKING ═══
    public List<Long> getChatTimes() { return chatTimes; }
    public void addChatTime(long t) { chatTimes.add(t); if (chatTimes.size() > 30) chatTimes.remove(0); }
    public List<Long> getCommandTimes() { return commandTimes; }
    public void addCommandTime(long t) { commandTimes.add(t); if (commandTimes.size() > 30) commandTimes.remove(0); }
    public List<Long> getDropTimes() { return dropTimes; }
    public void addDropTime(long t) { dropTimes.add(t); if (dropTimes.size() > 30) dropTimes.remove(0); }

    // ═══ PINGSPOOF TRACKING ═══
    public List<Integer> getRecentPings() { return recentPings; }
    public void addRecentPing(int p) { recentPings.add(p); if (recentPings.size() > 20) recentPings.remove(0); }

    // ═══ BADPACKETS TRACKING ═══
    public float getLastReportedPitch() { return lastReportedPitch; }
    public void setLastReportedPitch(float v) { this.lastReportedPitch = v; }
    public boolean getLastOnGround() { return lastOnGround; }
    public void setLastOnGround(boolean v) { this.lastOnGround = v; }
    public int getInvalidGroundTicks() { return invalidGroundTicks; }
    public void incrementInvalidGroundTicks() { this.invalidGroundTicks++; }
    public void resetInvalidGroundTicks() { this.invalidGroundTicks = 0; }

    // ═══ STRAFE TRACKING ═══
    public double getLastDeltaX() { return lastDeltaX; }
    public void setLastDeltaX(double v) { this.lastDeltaX = v; }
    public double getLastDeltaZ() { return lastDeltaZ; }
    public void setLastDeltaZ(double v) { this.lastDeltaZ = v; }
    public int getImpossibleStrafeCount() { return impossibleStrafeCount; }
    public void setImpossibleStrafeCount(int v) { this.impossibleStrafeCount = v; }

    // ═══ VCLIP TRACKING ═══
    public double getPreviousY() { return previousY; }
    public void setPreviousY(double v) { this.previousY = v; }

    // ═══ BACKTRACK TRACKING ═══
    public List<Location> getRecentAttackLocations() { return recentAttackLocations; }
    public void addRecentAttackLocation(Location l) { recentAttackLocations.add(l); if (recentAttackLocations.size() > 20) recentAttackLocations.remove(0); }
    public long getLastAttackLocationTime() { return lastAttackLocationTime; }
    public void setLastAttackLocationTime(long t) { this.lastAttackLocationTime = t; }

    // ═══ FAST PROJECTILE TRACKING ═══
    public List<Long> getProjectileThrowTimes() { return projectileThrowTimes; }
    public void addProjectileThrowTime(long t) { projectileThrowTimes.add(t); if (projectileThrowTimes.size() > 20) projectileThrowTimes.remove(0); }

    // ═══ MULTI AURA TRACKING ═══
    public Set<Integer> getAttackedEntitiesThisTick() { return attackedEntitiesThisTick; }
    public long getLastAttackTickTime() { return lastAttackTickTime; }
    public void setLastAttackTickTime(long t) { this.lastAttackTickTime = t; }

    // ═══ AUTO ARMOR / TOTEM TRACKING ═══
    public List<Long> getArmorEquipTimes() { return armorEquipTimes; }
    public void addArmorEquipTime(long t) { armorEquipTimes.add(t); if (armorEquipTimes.size() > 10) armorEquipTimes.remove(0); }
    public List<Long> getTotemSwapTimes() { return totemSwapTimes; }
    public void addTotemSwapTime(long t) { totemSwapTimes.add(t); if (totemSwapTimes.size() > 10) totemSwapTimes.remove(0); }

    // ═══ REGEN TRACKING ═══
    public List<Double> getHealthHistory() { return healthHistory; }
    public void addHealthHistory(double h) { healthHistory.add(h); if (healthHistory.size() > 40) healthHistory.remove(0); }
    public long getLastHealthCheckTime() { return lastHealthCheckTime; }
    public void setLastHealthCheckTime(long t) { this.lastHealthCheckTime = t; }

    // ═══ ANTIHUNGER TRACKING ═══
    public List<Integer> getHungerHistory() { return hungerHistory; }
    public void addHungerHistory(int h) { hungerHistory.add(h); if (hungerHistory.size() > 40) hungerHistory.remove(0); }
    public long getLastHungerCheckTime() { return lastHungerCheckTime; }
    public void setLastHungerCheckTime(long t) { this.lastHungerCheckTime = t; }
    public int getStaleHungerTicks() { return staleHungerTicks; }
    public void setStaleHungerTicks(int v) { this.staleHungerTicks = v; }

    // ═══ AUTOTOOL TRACKING ═══
    public List<Long> getToolSwitchTimes() { return toolSwitchTimes; }
    public void addToolSwitchTime(long t) { toolSwitchTimes.add(t); if (toolSwitchTimes.size() > 20) toolSwitchTimes.remove(0); }

    // ═══ XRAY TRACKING ═══
    public Map<String, Integer> getOreMinedCounts() { return oreMinedCounts; }
    public void incrementOreMined(String ore) { oreMinedCounts.merge(ore, 1, Integer::sum); }
    public int getTotalBlocksMined() { return totalBlocksMined; }
    public void incrementTotalBlocksMined() { this.totalBlocksMined++; }

    // ═══ TOWER TRACKING ═══
    public int getTowerPlaceCount() { return towerPlaceCount; }
    public void setTowerPlaceCount(int v) { this.towerPlaceCount = v; }
    public double getTowerStartY() { return towerStartY; }
    public void setTowerStartY(double v) { this.towerStartY = v; }
    public long getTowerWindowStart() { return towerWindowStart; }
    public void setTowerWindowStart(long t) { this.towerWindowStart = t; }

    // ═══ ANTILEVITATION TRACKING ═══
    public int getLevitationIgnoreTicks() { return levitationIgnoreTicks; }
    public void setLevitationIgnoreTicks(int v) { this.levitationIgnoreTicks = v; }

    // ═══ PACKET FLOOD TRACKING ═══
    public int getTotalPacketsPerSecond() { return totalPacketsPerSecond; }
    public void setTotalPacketsPerSecond(int v) { this.totalPacketsPerSecond = v; }
    public long getPacketFloodWindowStart() { return packetFloodWindowStart; }
    public void setPacketFloodWindowStart(long t) { this.packetFloodWindowStart = t; }

    // ═══ FREECAM TRACKING ═══
    public int getNoMovementWithRotationTicks() { return noMovementWithRotationTicks; }
    public void setNoMovementWithRotationTicks(int v) { this.noMovementWithRotationTicks = v; }

    // ═══ TARGET STRAFE TRACKING ═══
    public List<Double> getTargetStrafeAngles() { return targetStrafeAngles; }
    public void addTargetStrafeAngle(double a) { targetStrafeAngles.add(a); if (targetStrafeAngles.size() > 30) targetStrafeAngles.remove(0); }
    public double getLastTargetStrafeAngle() { return lastTargetStrafeAngle; }
    public void setLastTargetStrafeAngle(double v) { this.lastTargetStrafeAngle = v; }

    // ═══ WTAP TRACKING ═══
    public int getWTapCount() { return wTapCount; }
    public void setWTapCount(int v) { this.wTapCount = v; }
    public long getWTapWindowStart() { return wTapWindowStart; }
    public void setWTapWindowStart(long t) { this.wTapWindowStart = t; }

    // ═══ NOSLOWBOW TRACKING ═══
    public boolean isBowDrawing() { return bowDrawing; }
    public void setBowDrawing(boolean v) { this.bowDrawing = v; }

    // ═══ AUTOSWITCH TRACKING ═══
    public int getLastHeldSlot() { return lastHeldSlot; }
    public void setLastHeldSlot(int v) { this.lastHeldSlot = v; }
    public long getLastSlotSwitchTime() { return lastSlotSwitchTime; }
    public void setLastSlotSwitchTime(long t) { this.lastSlotSwitchTime = t; }
    public int getSlotSwitchBeforeAttackCount() { return slotSwitchBeforeAttackCount; }
    public void setSlotSwitchBeforeAttackCount(int v) { this.slotSwitchBeforeAttackCount = v; }

    // ═══ TPAURA TRACKING ═══
    public Location getLastPositionBeforeAttack() { return lastPositionBeforeAttack; }
    public void setLastPositionBeforeAttack(Location l) { this.lastPositionBeforeAttack = l; }
    public int getTpAuraFlags() { return tpAuraFlags; }
    public void setTpAuraFlags(int v) { this.tpAuraFlags = v; }

    // ═══ ANTIKB TRACKING ═══
    public long getLastKBReceiveTime() { return lastKBReceiveTime; }
    public void setLastKBReceiveTime(long t) { this.lastKBReceiveTime = t; }
    public boolean isKbPending() { return kbPending; }
    public void setKbPending(boolean v) { this.kbPending = v; }
    public double getKbExpectedHorizontal() { return kbExpectedHorizontal; }
    public void setKbExpectedHorizontal(double v) { this.kbExpectedHorizontal = v; }

    // ═══ CLICKAURA TRACKING ═══
    public int getBehindBackHitCount() { return behindBackHitCount; }
    public void setBehindBackHitCount(int v) { this.behindBackHitCount = v; }
    public long getClickAuraWindowStart() { return clickAuraWindowStart; }
    public void setClickAuraWindowStart(long t) { this.clickAuraWindowStart = t; }

    // ═══ REACH D TRACKING ═══
    public List<Double> getVerticalReachDistances() { return verticalReachDistances; }
    public void addVerticalReachDistance(double d) { verticalReachDistances.add(d); if (verticalReachDistances.size() > 20) verticalReachDistances.remove(0); }

    // ═══ AIMBOT TRACKING ═══
    public List<Float> getAimLockSamples() { return aimLockSamples; }
    public void addAimLockSample(float v) { aimLockSamples.add(v); if (aimLockSamples.size() > 40) aimLockSamples.remove(0); }
    public int getPerfectAimCount() { return perfectAimCount; }
    public void setPerfectAimCount(int v) { this.perfectAimCount = v; }
    public List<Double> getAimPredictionDeltas() { return aimPredictionDeltas; }
    public void addAimPredictionDelta(double d) { aimPredictionDeltas.add(d); if (aimPredictionDeltas.size() > 30) aimPredictionDeltas.remove(0); }

    // ═══ AIRJUMP TRACKING ═══
    public int getAirJumpCount() { return airJumpCount; }
    public void setAirJumpCount(int v) { this.airJumpCount = v; }
    public double getLastJumpY() { return lastJumpY; }
    public void setLastJumpY(double v) { this.lastJumpY = v; }

    // ═══ SPIDER TRACKING ═══
    public int getWallClimbTicks() { return wallClimbTicks; }
    public void setWallClimbTicks(int v) { this.wallClimbTicks = v; }

    // ═══ GLIDE TRACKING ═══
    public int getGlideTicks() { return glideTicks; }
    public void setGlideTicks(int v) { this.glideTicks = v; }
    public double getLastFallRate() { return lastFallRate; }
    public void setLastFallRate(double v) { this.lastFallRate = v; }

    // ═══ FASTCLIMB TRACKING ═══
    public double getClimbSpeed() { return climbSpeed; }
    public void setClimbSpeed(double v) { this.climbSpeed = v; }
    public int getClimbSpeedFlags() { return climbSpeedFlags; }
    public void setClimbSpeedFlags(int v) { this.climbSpeedFlags = v; }

    // ═══ BUNNYHOP TRACKING ═══
    public List<Double> getBhopSpeedHistory() { return bhopSpeedHistory; }
    public void addBhopSpeed(double s) { bhopSpeedHistory.add(s); if (bhopSpeedHistory.size() > 20) bhopSpeedHistory.remove(0); }
    public int getBhopJumpCount() { return bhopJumpCount; }
    public void setBhopJumpCount(int v) { this.bhopJumpCount = v; }

    // ═══ NOROTATE / ANTIAIM TRACKING ═══
    public double getLastMovementDirection() { return lastMovementDirection; }
    public void setLastMovementDirection(double v) { this.lastMovementDirection = v; }
    public int getNoRotateFlags() { return noRotateFlags; }
    public void setNoRotateFlags(int v) { this.noRotateFlags = v; }

    // ═══ DERP TRACKING ═══
    public int getRotationChangeCount() { return rotationChangeCount; }
    public void setRotationChangeCount(int v) { this.rotationChangeCount = v; }
    public long getRotationChangeWindowStart() { return rotationChangeWindowStart; }
    public void setRotationChangeWindowStart(long t) { this.rotationChangeWindowStart = t; }

    // ═══ AUTOFISH TRACKING ═══
    public List<Long> getFishCastTimes() { return fishCastTimes; }
    public void addFishCastTime(long t) { fishCastTimes.add(t); if (fishCastTimes.size() > 20) fishCastTimes.remove(0); }
    public List<Long> getFishReelTimes() { return fishReelTimes; }
    public void addFishReelTime(long t) { fishReelTimes.add(t); if (fishReelTimes.size() > 20) fishReelTimes.remove(0); }

    // ═══ INVENTORYCLICK TRACKING ═══
    public List<Long> getInventoryClickTimes() { return inventoryClickTimes; }
    public void addInventoryClickTime(long t) { inventoryClickTimes.add(t); if (inventoryClickTimes.size() > 30) inventoryClickTimes.remove(0); }

    // ═══ SKINBLINKER TRACKING ═══
    public int getSkinChangeCount() { return skinChangeCount; }
    public void setSkinChangeCount(int v) { this.skinChangeCount = v; }
    public long getSkinChangeWindowStart() { return skinChangeWindowStart; }
    public void setSkinChangeWindowStart(long t) { this.skinChangeWindowStart = t; }

    // ═══ ANTIAFK TRACKING ═══
    public long getLastActualInputTime() { return lastActualInputTime; }
    public void setLastActualInputTime(long t) { this.lastActualInputTime = t; }
    public int getAfkPatternCount() { return afkPatternCount; }
    public void setAfkPatternCount(int v) { this.afkPatternCount = v; }
    public List<Double> getAfkMovementAngles() { return afkMovementAngles; }
    public void addAfkMovementAngle(double a) { afkMovementAngles.add(a); if (afkMovementAngles.size() > 30) afkMovementAngles.remove(0); }

    // ═══ FASTHEAL TRACKING ═══
    public List<Long> getHealEventTimes() { return healEventTimes; }
    public void addHealEventTime(long t) { healEventTimes.add(t); if (healEventTimes.size() > 20) healEventTimes.remove(0); }

    // ═══ BOOKEXPLOIT TRACKING ═══
    public int getBookEditCount() { return bookEditCount; }
    public void setBookEditCount(int v) { this.bookEditCount = v; }
    public long getBookEditWindowStart() { return bookEditWindowStart; }
    public void setBookEditWindowStart(long t) { this.bookEditWindowStart = t; }

    // ═══ BARITONE TRACKING ═══
    public int getStraightLineCount() { return straightLineCount; }
    public void setStraightLineCount(int v) { this.straightLineCount = v; }
    public int getPathPatternCount() { return pathPatternCount; }
    public void setPathPatternCount(int v) { this.pathPatternCount = v; }
    public Location getLastPathPoint() { return lastPathPoint; }
    public void setLastPathPoint(Location l) { this.lastPathPoint = l; }

    // ═══ SELFDAMAGE TRACKING ═══
    public int getSelfDamageCount() { return selfDamageCount; }
    public void setSelfDamageCount(int v) { this.selfDamageCount = v; }
    public long getSelfDamageWindowStart() { return selfDamageWindowStart; }
    public void setSelfDamageWindowStart(long t) { this.selfDamageWindowStart = t; }

    // ═══ AUTOBLOCK TRACKING ═══
    public int getShieldToggleCount() { return shieldToggleCount; }
    public void setShieldToggleCount(int v) { this.shieldToggleCount = v; }
    public long getLastShieldToggleTime() { return lastShieldToggleTime; }
    public void setLastShieldToggleTime(long t) { this.lastShieldToggleTime = t; }
    public long getShieldToggleWindowStart() { return shieldToggleWindowStart; }
    public void setShieldToggleWindowStart(long t) { this.shieldToggleWindowStart = t; }

    // ═══ TRIGGERBOT TRACKING ═══
    public int getCrosshairAttackCount() { return crosshairAttackCount; }
    public void setCrosshairAttackCount(int v) { this.crosshairAttackCount = v; }
    public long getCrosshairTrackWindowStart() { return crosshairTrackWindowStart; }
    public void setCrosshairTrackWindowStart(long t) { this.crosshairTrackWindowStart = t; }

    // ═══ NOHITDELAY TRACKING ═══
    public long getLastEntityHurtTime() { return lastEntityHurtTime; }
    public void setLastEntityHurtTime(long t) { this.lastEntityHurtTime = t; }
    public int getConsecutiveImmediateHits() { return consecutiveImmediateHits; }
    public void setConsecutiveImmediateHits(int v) { this.consecutiveImmediateHits = v; }

    // ═══ EXTENDED CLICK ANALYSIS ═══
    public List<Long> getExtendedClickIntervals() { return extendedClickIntervals; }
    public void addExtendedClickInterval(long i) { extendedClickIntervals.add(i); if (extendedClickIntervals.size() > 100) extendedClickIntervals.remove(0); }

    // ═══ FIGHTBOT TRACKING ═══
    public int getCombatActionScore() { return combatActionScore; }
    public void setCombatActionScore(int v) { this.combatActionScore = v; }
    public long getCombatActionWindowStart() { return combatActionWindowStart; }
    public void setCombatActionWindowStart(long t) { this.combatActionWindowStart = t; }

    // ═══ ATTACKAURA TRACKING ═══
    public List<Integer> getTargetCycleOrder() { return targetCycleOrder; }
    public void addTargetCycleEntity(int id) { targetCycleOrder.add(id); if (targetCycleOrder.size() > 30) targetCycleOrder.remove(0); }

    // ═══ FLIGHT EXTENDED ═══
    public int getPacketFlightTicks() { return packetFlightTicks; }
    public void setPacketFlightTicks(int v) { this.packetFlightTicks = v; }
    public double getLastVerticalAcceleration() { return lastVerticalAcceleration; }
    public void setLastVerticalAcceleration(double v) { this.lastVerticalAcceleration = v; }
    public int getCreativeFlightTicks() { return creativeFlightTicks; }
    public void setCreativeFlightTicks(int v) { this.creativeFlightTicks = v; }

    // ═══ GROUNDSPOOF TRACKING ═══
    public int getGroundSpoofFlags() { return groundSpoofFlags; }
    public void setGroundSpoofFlags(int v) { this.groundSpoofFlags = v; }

    // ═══ TELEPORT TRACKING ═══
    public List<Double> getPositionJumps() { return positionJumps; }
    public void addPositionJump(double d) { positionJumps.add(d); if (positionJumps.size() > 20) positionJumps.remove(0); }

    // ═══ SAFEWALK TRACKING ═══
    public int getEdgeWalkCount() { return edgeWalkCount; }
    public void setEdgeWalkCount(int v) { this.edgeWalkCount = v; }
    public long getEdgeWalkWindowStart() { return edgeWalkWindowStart; }
    public void setEdgeWalkWindowStart(long t) { this.edgeWalkWindowStart = t; }

    // ═══ FASTSNEAK TRACKING ═══
    public double getLastSneakSpeed() { return lastSneakSpeed; }
    public void setLastSneakSpeed(double v) { this.lastSneakSpeed = v; }
    public int getFastSneakFlags() { return fastSneakFlags; }
    public void setFastSneakFlags(int v) { this.fastSneakFlags = v; }

    // ═══ FASTSWIM TRACKING ═══
    public double getLastSwimSpeed() { return lastSwimSpeed; }
    public void setLastSwimSpeed(double v) { this.lastSwimSpeed = v; }
    public int getFastSwimFlags() { return fastSwimFlags; }
    public void setFastSwimFlags(int v) { this.fastSwimFlags = v; }

    // ═══ BOATSPEED TRACKING ═══
    public double getLastBoatSpeed() { return lastBoatSpeed; }
    public void setLastBoatSpeed(double v) { this.lastBoatSpeed = v; }
    public int getBoatSpeedFlags() { return boatSpeedFlags; }
    public void setBoatSpeedFlags(int v) { this.boatSpeedFlags = v; }

    // ═══ HORSEJUMP TRACKING ═══
    public double getLastMountJumpHeight() { return lastMountJumpHeight; }
    public void setLastMountJumpHeight(double v) { this.lastMountJumpHeight = v; }
    public int getMountSpeedFlags() { return mountSpeedFlags; }
    public void setMountSpeedFlags(int v) { this.mountSpeedFlags = v; }

    // ═══ WATERWALK TRACKING ═══
    public int getWaterSurfaceTicks() { return waterSurfaceTicks; }
    public void setWaterSurfaceTicks(int v) { this.waterSurfaceTicks = v; }

    // ═══ ANTISLIP TRACKING ═══
    public int getIceMovementFlags() { return iceMovementFlags; }
    public void setIceMovementFlags(int v) { this.iceMovementFlags = v; }

    // ═══ AUTOJUMP TRACKING ═══
    public int getAutoJumpTicks() { return autoJumpTicks; }
    public void setAutoJumpTicks(int v) { this.autoJumpTicks = v; }
    public long getAutoJumpWindowStart() { return autoJumpWindowStart; }
    public void setAutoJumpWindowStart(long t) { this.autoJumpWindowStart = t; }

    // ═══ PHASEWALK TRACKING ═══
    public int getHorizontalPhaseFlags() { return horizontalPhaseFlags; }
    public void setHorizontalPhaseFlags(int v) { this.horizontalPhaseFlags = v; }

    // ═══ GRAVITYOVERRIDE TRACKING ═══
    public int getGravityViolationTicks() { return gravityViolationTicks; }
    public void setGravityViolationTicks(int v) { this.gravityViolationTicks = v; }

    // ═══ MOVEMENTPREDICTION TRACKING ═══
    public List<Double> getMovementPredictionDeltas() { return movementPredictionDeltas; }
    public void addMovementPredictionDelta(double d) { movementPredictionDeltas.add(d); if (movementPredictionDeltas.size() > 20) movementPredictionDeltas.remove(0); }

    // ═══ PROTOCOL TRACKING ═══
    public long getLastPacketSequenceTime() { return lastPacketSequenceTime; }
    public void setLastPacketSequenceTime(long t) { this.lastPacketSequenceTime = t; }
    public int getInvalidPacketSequenceCount() { return invalidPacketSequenceCount; }
    public void setInvalidPacketSequenceCount(int v) { this.invalidPacketSequenceCount = v; }

    // ═══ INVENTORY EXTENDED ═══
    public int getInventoryManipulationFlags() { return inventoryManipulationFlags; }
    public void setInventoryManipulationFlags(int v) { this.inventoryManipulationFlags = v; }
    public long getLastInventoryAction() { return lastInventoryAction; }
    public void setLastInventoryAction(long t) { this.lastInventoryAction = t; }

    // ═══ PACKETSPAM EXTENDED ═══
    public int getActionPacketsPerSecond() { return actionPacketsPerSecond; }
    public void setActionPacketsPerSecond(int v) { this.actionPacketsPerSecond = v; }
    public int getBlockPacketsPerSecond() { return blockPacketsPerSecond; }
    public void setBlockPacketsPerSecond(int v) { this.blockPacketsPerSecond = v; }

    // ═══ INTERACT TRACKING ═══
    public int getInvalidInteractCount() { return invalidInteractCount; }
    public void setInvalidInteractCount(int v) { this.invalidInteractCount = v; }

    // ═══ ACTIONSPOOF TRACKING ═══
    public int getActionSpoofCount() { return actionSpoofCount; }
    public void setActionSpoofCount(int v) { this.actionSpoofCount = v; }

    // ═══ NOROTATION TRACKING ═══
    public int getHeadFreezeCount() { return headFreezeCount; }
    public void setHeadFreezeCount(int v) { this.headFreezeCount = v; }

    // ═══ AUTOEAT TRACKING ═══
    public List<Long> getEatStartTimes() { return eatStartTimes; }
    public void addEatStartTime(long t) { eatStartTimes.add(t); if (eatStartTimes.size() > 20) eatStartTimes.remove(0); }

    // ═══ HEALTHSPOOF TRACKING ═══
    public double getLastServerHealth() { return lastServerHealth; }
    public void setLastServerHealth(double v) { this.lastServerHealth = v; }

    // ═══ IMPOSSIBLESLOT TRACKING ═══
    public int getImpossibleSlotAccessCount() { return impossibleSlotAccessCount; }
    public void setImpossibleSlotAccessCount(int v) { this.impossibleSlotAccessCount = v; }

    // ═══ ENTITYACTION TRACKING ═══
    public int getEntityActionSpoofCount() { return entityActionSpoofCount; }
    public void setEntityActionSpoofCount(int v) { this.entityActionSpoofCount = v; }

    // ═══ SERVERCRASHER TRACKING ═══
    public int getEntitySpawnCount() { return entitySpawnCount; }
    public void setEntitySpawnCount(int v) { this.entitySpawnCount = v; }
    public long getEntitySpawnWindowStart() { return entitySpawnWindowStart; }
    public void setEntitySpawnWindowStart(long t) { this.entitySpawnWindowStart = t; }
    public int getSignEditCount() { return signEditCount; }
    public void setSignEditCount(int v) { this.signEditCount = v; }
    public long getSignEditWindowStart() { return signEditWindowStart; }
    public void setSignEditWindowStart(long t) { this.signEditWindowStart = t; }

    // ═══ DUPEDETECT TRACKING ═══
    public int getSuspiciousDropCount() { return suspiciousDropCount; }
    public void setSuspiciousDropCount(int v) { this.suspiciousDropCount = v; }
    public long getSuspiciousDropWindowStart() { return suspiciousDropWindowStart; }
    public void setSuspiciousDropWindowStart(long t) { this.suspiciousDropWindowStart = t; }

    // ═══ NBTEXPLOIT TRACKING ═══
    public int getLargeNBTCount() { return largeNBTCount; }
    public void setLargeNBTCount(int v) { this.largeNBTCount = v; }

    // ═══ ENTITYEXPLOIT TRACKING ═══
    public int getVehicleStackCount() { return vehicleStackCount; }
    public void setVehicleStackCount(int v) { this.vehicleStackCount = v; }

    // ═══ CHUNKEXPLOIT TRACKING ═══
    public int getChunkRequestCount() { return chunkRequestCount; }
    public void setChunkRequestCount(int v) { this.chunkRequestCount = v; }
    public long getChunkRequestWindowStart() { return chunkRequestWindowStart; }
    public void setChunkRequestWindowStart(long t) { this.chunkRequestWindowStart = t; }

    // ═══ PAYLOADEXPLOIT TRACKING ═══
    public int getCustomPayloadCount() { return customPayloadCount; }
    public void setCustomPayloadCount(int v) { this.customPayloadCount = v; }
    public long getCustomPayloadWindowStart() { return customPayloadWindowStart; }
    public void setCustomPayloadWindowStart(long t) { this.customPayloadWindowStart = t; }

    // ═══ CONNECTION TRACKING ═══
    public int getReconnectCount() { return reconnectCount; }
    public void setReconnectCount(int v) { this.reconnectCount = v; }
    public long getLastDisconnectTime() { return lastDisconnectTime; }
    public void setLastDisconnectTime(long t) { this.lastDisconnectTime = t; }

    // ═══ PROTOCOLEXPLOIT TRACKING ═══
    public int getInvalidProtocolCount() { return invalidProtocolCount; }
    public void setInvalidProtocolCount(int v) { this.invalidProtocolCount = v; }

    // ═══ ITEMEXPLOIT TRACKING ═══
    public int getInvalidItemActionCount() { return invalidItemActionCount; }
    public void setInvalidItemActionCount(int v) { this.invalidItemActionCount = v; }

    // ═══ REDUCEDKB TRACKING ═══
    public double getKbActualHorizontal() { return kbActualHorizontal; }
    public void setKbActualHorizontal(double v) { this.kbActualHorizontal = v; }
    public int getPartialKBCount() { return partialKBCount; }
    public void setPartialKBCount(int v) { this.partialKBCount = v; }

    // ═══ HITMODIFIER TRACKING ═══
    public double getLastDamageDealt() { return lastDamageDealt; }
    public void setLastDamageDealt(double v) { this.lastDamageDealt = v; }
    public int getDamageModifierFlags() { return damageModifierFlags; }
    public void setDamageModifierFlags(int v) { this.damageModifierFlags = v; }

    // ═══ SPRINTRESET EXTENDED ═══
    public int getPerfectSprintResets() { return perfectSprintResets; }
    public void setPerfectSprintResets(int v) { this.perfectSprintResets = v; }
    public long getSprintResetWindowStart() { return sprintResetWindowStart; }
    public void setSprintResetWindowStart(long t) { this.sprintResetWindowStart = t; }

    // ═══ DAMAGEINDICATOR TRACKING ═══
    public int getDamageIndicatorFlags() { return damageIndicatorFlags; }
    public void setDamageIndicatorFlags(int v) { this.damageIndicatorFlags = v; }

    // ═══ COMBATANALYSIS TRACKING ═══
    public int getOverallCombatScore() { return overallCombatScore; }
    public void setOverallCombatScore(int v) { this.overallCombatScore = v; }
    public long getCombatSessionStart() { return combatSessionStart; }
    public void setCombatSessionStart(long t) { this.combatSessionStart = t; }

    // ═══ EXTENDED MISSING METHODS ═══

    // Packet time
    public long getLastPacketTime() { return lastPacketTime; }
    public void setLastPacketTime(long t) { this.lastPacketTime = t; }

    // Dig start time
    public long getLastDigStartTime() { return lastDigStartTime; }
    public void setLastDigStartTime(long t) { this.lastDigStartTime = t; }

    // Eat start time (single value)
    public long getEatStartTime() { return eatStartTime; }
    public void setEatStartTime(long t) { this.eatStartTime = t; }

    // Client brand
    public String getClientBrand() { return clientBrand; }
    public void setClientBrand(String brand) { this.clientBrand = brand; }

    // Animation time
    public long getLastAnimationTime() { return lastAnimationTime; }
    public void setLastAnimationTime(long t) { this.lastAnimationTime = t; }

    // Tick time
    public long getLastTickTime() { return lastTickTime; }
    public void setLastTickTime(long t) { this.lastTickTime = t; }

    // Interact time
    public long getLastInteractTime() { return lastInteractTime; }
    public void setLastInteractTime(long t) { this.lastInteractTime = t; }

    // Expected health
    public double getExpectedHealth() { return expectedHealth; }
    public void setExpectedHealth(double v) { this.expectedHealth = v; }

    // Entity action time
    public long getLastEntityActionTime() { return lastEntityActionTime; }
    public void setLastEntityActionTime(long t) { this.lastEntityActionTime = t; }

    // Bed state
    public boolean isInBed() { return inBed; }
    public void setInBed(boolean v) { this.inBed = v; }

    // Rotation mismatch streak
    public int getRotationMismatchStreak() { return rotationMismatchStreak; }
    public void incrementRotationMismatchStreak() { this.rotationMismatchStreak++; }
    public void resetRotationMismatchStreak() { this.rotationMismatchStreak = 0; }

    // Rotation frozen ticks
    public int getRotationFrozenTicks() { return rotationFrozenTicks; }
    public void incrementRotationFrozenTicks() { this.rotationFrozenTicks++; }
    public void resetRotationFrozenTicks() { this.rotationFrozenTicks = 0; }

    // Blocking state
    public boolean isBlocking() { return blocking; }
    public void setBlocking(boolean v) { this.blocking = v; }

    // Shift click time
    public long getLastShiftClickTime() { return lastShiftClickTime; }
    public void setLastShiftClickTime(long t) { this.lastShiftClickTime = t; }

    // Recorded health
    public double getLastRecordedHealth() { return lastRecordedHealth; }
    public void setLastRecordedHealth(double v) { this.lastRecordedHealth = v; }

    // Sneaking state
    public boolean isSneaking() { return sneaking; }
    public void setSneaking(boolean v) { this.sneaking = v; }

    // Sprinting state
    public boolean isSprinting() { return sprinting; }
    public void setSprinting(boolean v) { this.sprinting = v; }

    // Slot change time
    public long getLastSlotChangeTime() { return lastSlotChangeTime; }
    public void setLastSlotChangeTime(long t) { this.lastSlotChangeTime = t; }

    // Horse jumping
    public boolean isHorseJumping() { return horseJumping; }
    public void setHorseJumping(boolean v) { this.horseJumping = v; }

    // Move packet count
    public int getMovePacketCount() { return movePacketCount; }
    public void incrementMovePacketCount() { this.movePacketCount++; }
    public void resetMovePacketCount() { this.movePacketCount = 0; }

    // Timing deltas and variance
    public void addTimingDelta(long delta) { timingDeltas.add(delta); if (timingDeltas.size() > 50) timingDeltas.remove(0); }
    public double getTimingVariance() {
        if (timingDeltas.size() < 2) return 0.0;
        double mean = 0;
        for (long d : timingDeltas) mean += d;
        mean /= timingDeltas.size();
        double variance = 0;
        for (long d : timingDeltas) { double diff = d - mean; variance += diff * diff; }
        return variance / timingDeltas.size();
    }
    public int getTimingSampleCount() { return timingDeltas.size(); }

    // Consecutive eat count
    public int getConsecutiveEatCount() { return consecutiveEatCount; }
    public void incrementConsecutiveEatCount() { this.consecutiveEatCount++; }
    public void resetConsecutiveEatCount() { this.consecutiveEatCount = 0; }

    // Last eat time
    public long getLastEatTime() { return lastEatTime; }
    public void setLastEatTime(long t) { this.lastEatTime = t; }

    // Last move time
    public long getLastMoveTime() { return lastMoveTime; }
    public void setLastMoveTime(long t) { this.lastMoveTime = t; }

    // Last teleport time
    public long getLastTeleportTime() { return lastTeleportTime; }
    public void setLastTeleportTime(long t) { this.lastTeleportTime = t; }

    // Inventory click time
    public long getLastInventoryClickTime() { return lastInventoryClickTime; }
    public void setLastInventoryClickTime(long t) { this.lastInventoryClickTime = t; }

    // Inventory close time
    public long getLastInventoryCloseTime() { return lastInventoryCloseTime; }
    public void setLastInventoryCloseTime(long t) { this.lastInventoryCloseTime = t; }

    // Alias methods for naming mismatches
    public void addTargetCycleOrder(int id) { addTargetCycleEntity(id); }
    public void addRecentReachDistance(double d) { addReachDistance(d); }
    public boolean isLastOnGround() { return lastOnGround; }
    public void setInvalidGroundTicks(int v) { this.invalidGroundTicks = v; }
    public void setSprintToggleCount(int v) { this.sprintToggleCount = v; }
    public void setLastYaw(float v) { this.lastYaw = v; }
    public void setLastPitch(float v) { this.lastPitch = v; }
}
