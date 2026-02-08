package com.yourdomain.guardianac.checks;

import com.yourdomain.guardianac.GuardianAC;
// Movement
import com.yourdomain.guardianac.checks.movement.*;
import com.yourdomain.guardianac.checks.movement.flight.*;
import com.yourdomain.guardianac.checks.movement.phasewalk.*;
import com.yourdomain.guardianac.checks.movement.noslowmotion.*;
import com.yourdomain.guardianac.checks.movement.faststair.*;
import com.yourdomain.guardianac.checks.movement.antisneak.*;
import com.yourdomain.guardianac.checks.movement.gravityoverride.*;
import com.yourdomain.guardianac.checks.movement.antislip.*;
import com.yourdomain.guardianac.checks.movement.teleport.*;
import com.yourdomain.guardianac.checks.movement.autojump.*;
import com.yourdomain.guardianac.checks.movement.waterwalk.*;
import com.yourdomain.guardianac.checks.movement.safewalk.*;
import com.yourdomain.guardianac.checks.movement.boatspeed.*;
import com.yourdomain.guardianac.checks.movement.fastsneak.*;
import com.yourdomain.guardianac.checks.movement.movementprediction.*;
import com.yourdomain.guardianac.checks.movement.packetmotion.*;
import com.yourdomain.guardianac.checks.movement.horsejump.*;
import com.yourdomain.guardianac.checks.movement.fastswim.*;
import com.yourdomain.guardianac.checks.movement.parkour.*;
import com.yourdomain.guardianac.checks.movement.groundspoof.*;
// Combat
import com.yourdomain.guardianac.checks.combat.*;
import com.yourdomain.guardianac.checks.combat.killaura.*;
import com.yourdomain.guardianac.checks.combat.reach.*;
import com.yourdomain.guardianac.checks.combat.velocity.*;
import com.yourdomain.guardianac.checks.combat.autoclicker.*;
import com.yourdomain.guardianac.checks.combat.aimassist.*;
import com.yourdomain.guardianac.checks.combat.hitbox.*;
import com.yourdomain.guardianac.checks.combat.aimbot.*;
import com.yourdomain.guardianac.checks.combat.autoblock.*;
import com.yourdomain.guardianac.checks.combat.triggerbot.*;
import com.yourdomain.guardianac.checks.combat.reducedkb.*;
import com.yourdomain.guardianac.checks.combat.sprintreset.*;
import com.yourdomain.guardianac.checks.combat.nohitdelay.*;
import com.yourdomain.guardianac.checks.combat.hitmodifier.*;
import com.yourdomain.guardianac.checks.combat.fightbot.*;
import com.yourdomain.guardianac.checks.combat.attackaura.*;
import com.yourdomain.guardianac.checks.combat.damageindicator.*;
import com.yourdomain.guardianac.checks.combat.clickpattern.*;
// Player
import com.yourdomain.guardianac.checks.player.*;
import com.yourdomain.guardianac.checks.player.badpackets.*;
import com.yourdomain.guardianac.checks.player.inventory.*;
import com.yourdomain.guardianac.checks.player.animation.*;
import com.yourdomain.guardianac.checks.player.impossibleslot.*;
import com.yourdomain.guardianac.checks.player.entityaction.*;
import com.yourdomain.guardianac.checks.player.norotation.*;
import com.yourdomain.guardianac.checks.player.actionspoof.*;
import com.yourdomain.guardianac.checks.player.packetspam.*;
import com.yourdomain.guardianac.checks.player.protocol.*;
import com.yourdomain.guardianac.checks.player.interact.*;
import com.yourdomain.guardianac.checks.player.timing.*;
import com.yourdomain.guardianac.checks.player.healthspoof.*;
import com.yourdomain.guardianac.checks.player.autoeat.*;
// Exploit
import com.yourdomain.guardianac.checks.exploit.*;
import com.yourdomain.guardianac.checks.exploit.servercrasher.*;
import com.yourdomain.guardianac.checks.exploit.dupedetect.*;
import com.yourdomain.guardianac.checks.exploit.entityexploit.*;
import com.yourdomain.guardianac.checks.exploit.nbtexploit.*;
import com.yourdomain.guardianac.checks.exploit.payloadexploit.*;
import com.yourdomain.guardianac.checks.exploit.chunkexploit.*;

import java.util.ArrayList;
import java.util.List;

public class CheckManager {

    private final GuardianAC plugin;
    private final List<Check> checks;

    public CheckManager(GuardianAC plugin) {
        this.plugin = plugin;
        this.checks = new ArrayList<>();
    }

    public void registerChecks() {
        // ══════════════════════════════════════════════════════
        // ── Movement checks (70 total) ──
        // ══════════════════════════════════════════════════════
        checks.add(new FlyCheck(plugin));
        checks.add(new SpeedCheck(plugin));
        checks.add(new ScaffoldCheck(plugin));
        checks.add(new StepCheck(plugin));
        checks.add(new LongJumpCheck(plugin));
        checks.add(new InventoryMoveCheck(plugin));
        checks.add(new ElytraFlyCheck(plugin));
        checks.add(new BoatFlyCheck(plugin));
        checks.add(new AntiVoidCheck(plugin));
        checks.add(new BlinkCheck(plugin));
        checks.add(new NoWebCheck(plugin));
        checks.add(new HighJumpCheck(plugin));
        checks.add(new VClipCheck(plugin));
        checks.add(new StrafeCheck(plugin));
        checks.add(new EntitySpeedCheck(plugin));
        checks.add(new InvalidSprintCheck(plugin));
        checks.add(new AirJumpCheck(plugin));
        checks.add(new SpiderCheck(plugin));
        checks.add(new GlideCheck(plugin));
        checks.add(new FastClimbCheck(plugin));
        checks.add(new BunnyHopCheck(plugin));
        checks.add(new NoRotateCheck(plugin));
        checks.add(new WallPhaseCheck(plugin));
        checks.add(new NoSlowdownCheck(plugin));
        checks.add(new JesusFloatCheck(plugin));
        checks.add(new MotionSpoofCheck(plugin));
        // Flight A-F
        checks.add(new FlightA(plugin));
        checks.add(new FlightB(plugin));
        checks.add(new FlightC(plugin));
        checks.add(new FlightD(plugin));
        checks.add(new FlightE(plugin));
        checks.add(new FlightF(plugin));
        // PhaseWalk A-C
        checks.add(new PhaseWalkA(plugin));
        checks.add(new PhaseWalkB(plugin));
        checks.add(new PhaseWalkC(plugin));
        // NoSlowMotion A-C
        checks.add(new NoSlowMotionA(plugin));
        checks.add(new NoSlowMotionB(plugin));
        checks.add(new NoSlowMotionC(plugin));
        // FastStair A-B
        checks.add(new FastStairA(plugin));
        checks.add(new FastStairB(plugin));
        // AntiSneak A-B
        checks.add(new AntiSneakA(plugin));
        checks.add(new AntiSneakB(plugin));
        // GravityOverride A-C
        checks.add(new GravityOverrideA(plugin));
        checks.add(new GravityOverrideB(plugin));
        checks.add(new GravityOverrideC(plugin));
        // AntiSlip A-B
        checks.add(new AntiSlipA(plugin));
        checks.add(new AntiSlipB(plugin));
        // Teleport A-D
        checks.add(new TeleportA(plugin));
        checks.add(new TeleportB(plugin));
        checks.add(new TeleportC(plugin));
        checks.add(new TeleportD(plugin));
        // AutoJump A-B
        checks.add(new AutoJumpA(plugin));
        checks.add(new AutoJumpB(plugin));
        // WaterWalk A-B
        checks.add(new WaterWalkA(plugin));
        checks.add(new WaterWalkB(plugin));
        // SafeWalk A-B
        checks.add(new SafeWalkA(plugin));
        checks.add(new SafeWalkB(plugin));
        // BoatSpeed A-B
        checks.add(new BoatSpeedA(plugin));
        checks.add(new BoatSpeedB(plugin));
        // FastSneak A-B
        checks.add(new FastSneakA(plugin));
        checks.add(new FastSneakB(plugin));
        // MovementPrediction A-B
        checks.add(new MovementPredictionA(plugin));
        checks.add(new MovementPredictionB(plugin));
        // PacketMotion A-B
        checks.add(new PacketMotionA(plugin));
        checks.add(new PacketMotionB(plugin));
        // HorseJump A-B
        checks.add(new HorseJumpA(plugin));
        checks.add(new HorseJumpB(plugin));
        // FastSwim A-C
        checks.add(new FastSwimA(plugin));
        checks.add(new FastSwimB(plugin));
        checks.add(new FastSwimC(plugin));
        // Parkour A-B
        checks.add(new ParkourA(plugin));
        checks.add(new ParkourB(plugin));
        // GroundSpoof A-C
        checks.add(new GroundSpoofA(plugin));
        checks.add(new GroundSpoofB(plugin));
        checks.add(new GroundSpoofC(plugin));

        // ══════════════════════════════════════════════════════
        // ── Combat checks (109 total) ──
        // ══════════════════════════════════════════════════════
        // KillAura A-V
        checks.add(new KillAuraA(plugin));
        checks.add(new KillAuraB(plugin));
        checks.add(new KillAuraC(plugin));
        checks.add(new KillAuraD(plugin));
        checks.add(new KillAuraE(plugin));
        checks.add(new KillAuraF(plugin));
        checks.add(new KillAuraG(plugin));
        checks.add(new KillAuraH(plugin));
        checks.add(new KillAuraI(plugin));
        checks.add(new KillAuraJ(plugin));
        checks.add(new KillAuraK(plugin));
        checks.add(new KillAuraL(plugin));
        checks.add(new KillAuraM(plugin));
        checks.add(new KillAuraN(plugin));
        checks.add(new KillAuraO(plugin));
        checks.add(new KillAuraP(plugin));
        checks.add(new KillAuraQ(plugin));
        checks.add(new KillAuraR(plugin));
        checks.add(new KillAuraS(plugin));
        checks.add(new KillAuraT(plugin));
        checks.add(new KillAuraU(plugin));
        checks.add(new KillAuraV(plugin));
        // Reach A-J
        checks.add(new ReachA(plugin));
        checks.add(new ReachB(plugin));
        checks.add(new ReachC(plugin));
        checks.add(new ReachD(plugin));
        checks.add(new ReachE(plugin));
        checks.add(new ReachF(plugin));
        checks.add(new ReachG(plugin));
        checks.add(new ReachH(plugin));
        checks.add(new ReachI(plugin));
        checks.add(new ReachJ(plugin));
        // Velocity A-G
        checks.add(new VelocityA(plugin));
        checks.add(new VelocityB(plugin));
        checks.add(new VelocityC(plugin));
        checks.add(new VelocityD(plugin));
        checks.add(new VelocityE(plugin));
        checks.add(new VelocityF(plugin));
        checks.add(new VelocityG(plugin));
        // AutoClicker A-G
        checks.add(new AutoClickerA(plugin));
        checks.add(new AutoClickerB(plugin));
        checks.add(new AutoClickerC(plugin));
        checks.add(new AutoClickerD(plugin));
        checks.add(new AutoClickerE(plugin));
        checks.add(new AutoClickerF(plugin));
        checks.add(new AutoClickerG(plugin));
        // AimAssist A-G
        checks.add(new AimAssistA(plugin));
        checks.add(new AimAssistB(plugin));
        checks.add(new AimAssistC(plugin));
        checks.add(new AimAssistD(plugin));
        checks.add(new AimAssistE(plugin));
        checks.add(new AimAssistF(plugin));
        checks.add(new AimAssistG(plugin));
        // HitBox A-F
        checks.add(new HitBoxA(plugin));
        checks.add(new HitBoxB(plugin));
        checks.add(new HitBoxC(plugin));
        checks.add(new HitBoxD(plugin));
        checks.add(new HitBoxE(plugin));
        checks.add(new HitBoxF(plugin));
        // Aimbot A-H
        checks.add(new AimbotA(plugin));
        checks.add(new AimbotB(plugin));
        checks.add(new AimbotC(plugin));
        checks.add(new AimbotD(plugin));
        checks.add(new AimbotE(plugin));
        checks.add(new AimbotF(plugin));
        checks.add(new AimbotG(plugin));
        checks.add(new AimbotH(plugin));
        // AutoBlock A-C
        checks.add(new AutoBlockA(plugin));
        checks.add(new AutoBlockB(plugin));
        checks.add(new AutoBlockC(plugin));
        // TriggerBot A-D
        checks.add(new TriggerBotA(plugin));
        checks.add(new TriggerBotB(plugin));
        checks.add(new TriggerBotC(plugin));
        checks.add(new TriggerBotD(plugin));
        // ReducedKB A-C
        checks.add(new ReducedKBA(plugin));
        checks.add(new ReducedKBB(plugin));
        checks.add(new ReducedKBC(plugin));
        // SprintReset A-C
        checks.add(new SprintResetA(plugin));
        checks.add(new SprintResetB(plugin));
        checks.add(new SprintResetC(plugin));
        // NoHitDelay A-C
        checks.add(new NoHitDelayA(plugin));
        checks.add(new NoHitDelayB(plugin));
        checks.add(new NoHitDelayC(plugin));
        // HitModifier A-C
        checks.add(new HitModifierA(plugin));
        checks.add(new HitModifierB(plugin));
        checks.add(new HitModifierC(plugin));
        // FightBot A-C
        checks.add(new FightBotA(plugin));
        checks.add(new FightBotB(plugin));
        checks.add(new FightBotC(plugin));
        // AttackAura A-C
        checks.add(new AttackAuraA(plugin));
        checks.add(new AttackAuraB(plugin));
        checks.add(new AttackAuraC(plugin));
        // DamageIndicator A-C
        checks.add(new DamageIndicatorA(plugin));
        checks.add(new DamageIndicatorB(plugin));
        checks.add(new DamageIndicatorC(plugin));
        // ClickPattern A-D
        checks.add(new ClickPatternA(plugin));
        checks.add(new ClickPatternB(plugin));
        checks.add(new ClickPatternC(plugin));
        checks.add(new ClickPatternD(plugin));
        // Combat misc
        checks.add(new CriticalsCheck(plugin));
        checks.add(new SilentAimCheck(plugin));
        checks.add(new HitRateCheck(plugin));
        checks.add(new SwingFrequencyCheck(plugin));
        checks.add(new WeaponSwitchCheck(plugin));
        checks.add(new FastBowCheck(plugin));
        checks.add(new NoSwingCheck(plugin));
        checks.add(new BacktrackCheck(plugin));
        checks.add(new FastProjectileCheck(plugin));
        checks.add(new MultiAuraCheck(plugin));
        checks.add(new TargetStrafeCheck(plugin));
        checks.add(new WTapCheck(plugin));
        checks.add(new NoSlowBowCheck(plugin));
        checks.add(new AutoSwitchCheck(plugin));
        checks.add(new TPAuraCheck(plugin));
        checks.add(new AntiKBCheck(plugin));
        checks.add(new ClickAuraCheck(plugin));

        // ══════════════════════════════════════════════════════
        // ── Player checks (58 total) ──
        // ══════════════════════════════════════════════════════
        checks.add(new NoSlowCheck(plugin));
        checks.add(new NoFallCheck(plugin));
        checks.add(new FastEatCheck(plugin));
        checks.add(new FastBreakCheck(plugin));
        checks.add(new FastPlaceCheck(plugin));
        checks.add(new ChestStealerCheck(plugin));
        checks.add(new NukerCheck(plugin));
        checks.add(new AutoArmorCheck(plugin));
        checks.add(new AutoTotemCheck(plugin));
        checks.add(new RegenCheck(plugin));
        checks.add(new AntiHungerCheck(plugin));
        checks.add(new GhostHandCheck(plugin));
        checks.add(new AutoToolCheck(plugin));
        checks.add(new DerpCheck(plugin));
        checks.add(new AutoFishCheck(plugin));
        checks.add(new InventoryClickCheck(plugin));
        checks.add(new SkinBlinkerCheck(plugin));
        checks.add(new AntiAFKCheck(plugin));
        checks.add(new FastHealCheck(plugin));
        checks.add(new FastCraftCheck(plugin));
        checks.add(new PortalGlitchCheck(plugin));
        checks.add(new ContainerActionCheck(plugin));
        // BadPackets A-H
        checks.add(new BadPacketsA(plugin));
        checks.add(new BadPacketsB(plugin));
        checks.add(new BadPacketsC(plugin));
        checks.add(new BadPacketsD(plugin));
        checks.add(new BadPacketsE(plugin));
        checks.add(new BadPacketsF(plugin));
        checks.add(new BadPacketsG(plugin));
        checks.add(new BadPacketsH(plugin));
        // Inventory A-E
        checks.add(new InventoryA(plugin));
        checks.add(new InventoryB(plugin));
        checks.add(new InventoryC(plugin));
        checks.add(new InventoryD(plugin));
        checks.add(new InventoryE(plugin));
        // Animation A-B
        checks.add(new AnimationA(plugin));
        checks.add(new AnimationB(plugin));
        // ImpossibleSlot A-B
        checks.add(new ImpossibleSlotA(plugin));
        checks.add(new ImpossibleSlotB(plugin));
        // EntityAction A-E
        checks.add(new EntityActionA(plugin));
        checks.add(new EntityActionB(plugin));
        checks.add(new EntityActionC(plugin));
        checks.add(new EntityActionD(plugin));
        checks.add(new EntityActionE(plugin));
        // NoRotation A-B
        checks.add(new NoRotationA(plugin));
        checks.add(new NoRotationB(plugin));
        // ActionSpoof A-C
        checks.add(new ActionSpoofA(plugin));
        checks.add(new ActionSpoofB(plugin));
        checks.add(new ActionSpoofC(plugin));
        // PacketSpam A-C
        checks.add(new PacketSpamA(plugin));
        checks.add(new PacketSpamB(plugin));
        checks.add(new PacketSpamC(plugin));
        // Protocol A-E
        checks.add(new ProtocolA(plugin));
        checks.add(new ProtocolB(plugin));
        checks.add(new ProtocolC(plugin));
        checks.add(new ProtocolD(plugin));
        checks.add(new ProtocolE(plugin));
        // Interact A-C
        checks.add(new InteractA(plugin));
        checks.add(new InteractB(plugin));
        checks.add(new InteractC(plugin));
        // Timing A-B
        checks.add(new TimingA(plugin));
        checks.add(new TimingB(plugin));
        // HealthSpoof A-B
        checks.add(new HealthSpoofA(plugin));
        checks.add(new HealthSpoofB(plugin));
        // AutoEat A-B
        checks.add(new AutoEatA(plugin));
        checks.add(new AutoEatB(plugin));

        // ══════════════════════════════════════════════════════
        // ── Exploit checks (41 total) ──
        // ══════════════════════════════════════════════════════
        checks.add(new TimerCheck(plugin));
        checks.add(new PhaseCheck(plugin));
        checks.add(new JesusCheck(plugin));
        checks.add(new ChatSpamCheck(plugin));
        checks.add(new CommandSpamCheck(plugin));
        checks.add(new ItemSpamCheck(plugin));
        checks.add(new IllegalBlockCheck(plugin));
        checks.add(new PingSpoofCheck(plugin));
        checks.add(new CrasherCheck(plugin));
        checks.add(new XrayDetectorCheck(plugin));
        checks.add(new TowerCheck(plugin));
        checks.add(new PacketFloodCheck(plugin));
        checks.add(new FreeCamCheck(plugin));
        checks.add(new AntiLevitationCheck(plugin));
        checks.add(new ChestAuraCheck(plugin));
        checks.add(new DisablerCheck(plugin));
        checks.add(new BookExploitCheck(plugin));
        checks.add(new NameSpoofCheck(plugin));
        checks.add(new BaritoneCheck(plugin));
        checks.add(new SelfDamageCheck(plugin));
        checks.add(new WorldDownloaderCheck(plugin));
        checks.add(new ExploitFixerCheck(plugin));
        checks.add(new MemoryFloodCheck(plugin));
        checks.add(new InvalidSlotCheck(plugin));
        // ServerCrasher A-F
        checks.add(new ServerCrasherA(plugin));
        checks.add(new ServerCrasherB(plugin));
        checks.add(new ServerCrasherC(plugin));
        checks.add(new ServerCrasherD(plugin));
        checks.add(new ServerCrasherE(plugin));
        checks.add(new ServerCrasherF(plugin));
        // DupeDetect A-D
        checks.add(new DupeDetectA(plugin));
        checks.add(new DupeDetectB(plugin));
        checks.add(new DupeDetectC(plugin));
        checks.add(new DupeDetectD(plugin));
        // EntityExploit A-C
        checks.add(new EntityExploitA(plugin));
        checks.add(new EntityExploitB(plugin));
        checks.add(new EntityExploitC(plugin));
        // NBTExploit A-C
        checks.add(new NBTExploitA(plugin));
        checks.add(new NBTExploitB(plugin));
        checks.add(new NBTExploitC(plugin));
        // PayloadExploit A
        checks.add(new PayloadExploitA(plugin));
        // ChunkExploit A-B
        checks.add(new ChunkExploitA(plugin));
        checks.add(new ChunkExploitB(plugin));

        for (Check check : checks) {
            plugin.getServer().getPluginManager().registerEvents(check, plugin);
        }

        plugin.getLogger().info("Registered " + checks.size() + " checks.");
    }

    public List<Check> getChecks() {
        return checks;
    }

    public Check getCheckByName(String name) {
        for (Check check : checks) {
            if (check.getDisplayName().equalsIgnoreCase(name)
                    || check.getViolationKey().equalsIgnoreCase(name)) {
                return check;
            }
        }
        return null;
    }

    /** Get all checks that share a base name (e.g. "KillAura" returns A-J). */
    public List<Check> getChecksByBaseName(String baseName) {
        List<Check> result = new ArrayList<>();
        for (Check check : checks) {
            if (check.getName().equalsIgnoreCase(baseName)) {
                result.add(check);
            }
        }
        return result;
    }
}
