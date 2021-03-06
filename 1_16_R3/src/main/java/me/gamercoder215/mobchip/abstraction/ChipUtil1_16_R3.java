package me.gamercoder215.mobchip.abstraction;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Lifecycle;
import me.gamercoder215.mobchip.EntityBody;
import me.gamercoder215.mobchip.ai.animation.EntityAnimation;
import me.gamercoder215.mobchip.ai.attribute.Attribute;
import me.gamercoder215.mobchip.ai.attribute.AttributeInstance;
import me.gamercoder215.mobchip.ai.behavior.BehaviorResult;
import me.gamercoder215.mobchip.ai.controller.EntityController;
import me.gamercoder215.mobchip.ai.enderdragon.CustomPhase;
import me.gamercoder215.mobchip.ai.goal.Pathfinder;
import me.gamercoder215.mobchip.ai.goal.*;
import me.gamercoder215.mobchip.ai.goal.target.*;
import me.gamercoder215.mobchip.ai.gossip.EntityGossipContainer;
import me.gamercoder215.mobchip.ai.gossip.GossipType;
import me.gamercoder215.mobchip.ai.memories.Memory;
import me.gamercoder215.mobchip.ai.navigation.EntityNavigation;
import me.gamercoder215.mobchip.ai.navigation.NavigationPath;
import me.gamercoder215.mobchip.ai.schedule.EntityScheduleManager;
import me.gamercoder215.mobchip.util.Position;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.World;
import org.bukkit.*;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftSound;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.attribute.CraftAttributeInstance;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.entity.*;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftNamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.bukkit.event.entity.EntityDamageEvent.DamageCause.*;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ChipUtil1_16_R3 implements ChipUtil {

    private static ItemStack fromNMS(net.minecraft.server.v1_16_R3.ItemStack item) { return CraftItemStack.asBukkitCopy(item); }

    @Override
    public void addCustomPathfinder(CustomPathfinder p, int priority, boolean target) {
        Mob m = p.getEntity();
        EntityInsentient mob = toNMS(m);
        PathfinderGoalSelector s = target ? mob.targetSelector : mob.goalSelector;

        PathfinderGoal g = new PathfinderGoal() {
            @Override
            public boolean a() {
                return p.canStart();
            }
            @Override
            public boolean b() {
                return p.canContinueToUse();
            }
            @Override
            public boolean C_() {
                return p.canInterrupt();
            }

            @Override
            public void c() {
                p.start();
            }

            @Override
            public void e() {
                p.tick();
            }

        };

        Pathfinder.PathfinderFlag[] flags = p.getFlags() == null ? new Pathfinder.PathfinderFlag[0] : p.getFlags();
        for (Pathfinder.PathfinderFlag f : flags) {
            EnumSet<PathfinderGoal.Type> nmsFlags = g.i() == null ? EnumSet.allOf(PathfinderGoal.Type.class) : EnumSet.copyOf(g.i());
            nmsFlags.add(toNMS(f));
            g.a(nmsFlags);
        }

        s.a(priority, g);
    }

    @Override
    public Set<WrappedPathfinder> getGoals(Mob m, boolean target) {
        EntityInsentient mob = toNMS(m);
        PathfinderGoalSelector s = target ? mob.targetSelector : mob.goalSelector;

        Set<WrappedPathfinder> pF = new HashSet<>();

        try {
            Field f = s.getClass().getDeclaredField("d");
            f.setAccessible(true);
            Set<PathfinderGoalWrapped> goals = (Set<PathfinderGoalWrapped>) f.get(s);
           goals.forEach(w -> pF.add(new WrappedPathfinder(fromNMS(w.j()), w.h())));
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getMessage());
            for (StackTraceElement e1 : e.getStackTrace()) {
                Bukkit.getLogger().severe(e1.toString());
            }
        }

        return pF;
    }

    @Override
    public Collection<WrappedPathfinder> getRunningGoals(Mob m, boolean target) {
        EntityInsentient mob = toNMS(m);
        PathfinderGoalSelector s = target ? mob.targetSelector : mob.goalSelector;

        Collection<WrappedPathfinder> l = new HashSet<>();
        s.d().forEach(w -> l.add(new WrappedPathfinder(fromNMS(w.j()), w.h())));

        return l;
    }

    @Override
    public void setFlag(Mob m, Pathfinder.PathfinderFlag flag, boolean target, boolean value) {
        EntityInsentient mob = toNMS(m);
        PathfinderGoalSelector s = target ? mob.targetSelector : mob.goalSelector;
        s.a(toNMS(flag), value);
    }

    private static Class<? extends EntityLiving> toNMS(Class<? extends LivingEntity> clazz) {
        try {
            Method m = clazz.getDeclaredMethod("getHandle");
            return m.getReturnType().asSubclass(EntityLiving.class);
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) Bukkit.getLogger().severe(s.toString());

            return null;
        }
    }

    private static net.minecraft.server.v1_16_R3.ItemStack toNMS(ItemStack i) {
        return CraftItemStack.asNMSCopy(i);
    }

    private static SoundEffect toNMS(Sound s) {
        return CraftSound.getSoundEffect(s);
    }

    private static PathfinderGoal toNMS(Pathfinder b) {
        Mob mob = b.getEntity();
        EntityInsentient m = toNMS(mob);

        final PathfinderGoal g;
        switch (b.getInternalName()) {
            case "AvoidTarget": {
                PathfinderAvoidEntity<?> p = (PathfinderAvoidEntity<?>) b;
                g = new PathfinderGoalAvoidTarget<>((EntityCreature) m, toNMS(p.getFilter()), p.getMaxDistance(), p.getSpeedModifier(), p.getSprintModifier());
                break;
            }
            case "ArrowAttack": {
                PathfinderRangedAttack p = (PathfinderRangedAttack) b;
                g = new PathfinderGoalArrowAttack((IRangedEntity) m, p.getSpeedModifier(), p.getMinAttackInterval(), p.getMaxAttackInterval(), p.getRange());
                break;
            }
            case "Beg": {
                PathfinderBeg p = (PathfinderBeg) b;
                g = new PathfinderGoalBeg((EntityWolf) m, p.getRange());
                break;
            }
            case "BowShoot": {
                PathfinderRangedBowAttack p = (PathfinderRangedBowAttack) b;
                g = new PathfinderGoalBowShoot((EntityMonster) m, p.getSpeedModifier(), p.getInterval(), p.getRange());
                break;
            }
            case "BreakDoor": {
                PathfinderBreakDoor p = (PathfinderBreakDoor) b;
                g = new PathfinderGoalBreakDoor(m, p.getBreakTime(), d -> p.getCondition().test(fromNMS(d)));
                break;
            }
            case "Breath": {
                PathfinderBreathAir p = (PathfinderBreathAir) b;
                g = new PathfinderGoalBreath((EntityCreature) m);
                break;
            }
            case "Breed": {
                PathfinderBreed p = (PathfinderBreed) b;
                g = new PathfinderGoalBreed((EntityAnimal) m, p.getSpeedModifier());
                break;
            }
            case "CatSitOnBed": {
                PathfinderCatOnBed p = (PathfinderCatOnBed) b;
                g = new PathfinderGoalCatSitOnBed((EntityCat) m, p.getSpeedModifier(), Math.min((int) p.getRange(), 1));
                break;
            }
            case "CrossbowAttack": {
                PathfinderRangedCrossbowAttack p = (PathfinderRangedCrossbowAttack) b;
                g = new PathfinderGoalCrossbowAttack((EntityMonster) m, p.getSpeedModifier(), p.getRange());
                break;
            }
            case "DoorOpen": {
                PathfinderOpenDoor p = (PathfinderOpenDoor) b;
                g = new PathfinderGoalDoorOpen(m, p.mustClose());
                break;
            }
            case "EatTile": {
                PathfinderEatTile p = (PathfinderEatTile) b;
                g = new PathfinderGoalEatTile(m);
                break;
            }
            case "FishSchool": {
                PathfinderFollowFishLeader p = (PathfinderFollowFishLeader) b;
                g = new PathfinderGoalFishSchool((EntityFishSchool) m);
                break;
            }
            case "FleeSun": {
                PathfinderFleeSun p = (PathfinderFleeSun) b;
                g = new PathfinderGoalFleeSun((EntityCreature) m, p.getSpeedModifier());
                break;
            }
            case "Float": {
                PathfinderFloat p = (PathfinderFloat) b;
                g = new PathfinderGoalFloat(m);
                break;
            }
            case "FollowBoat": {
                PathfinderFollowBoat p = (PathfinderFollowBoat) b;
                g = new PathfinderGoalFollowBoat((EntityCreature) m);
                break;
            }
            case "FollowEntity": {
                PathfinderFollowMob p = (PathfinderFollowMob) b;
                g = new PathfinderGoalFollowEntity(m, p.getSpeedModifier(), p.getStopDistance(), p.getRange());
                break;
            }
            case "FollowOwner": {
                PathfinderFollowOwner p = (PathfinderFollowOwner) b;
                g = new PathfinderGoalFollowOwner((EntityTameableAnimal) m, p.getSpeedModifier(), p.getStartDistance(), p.getStopDistance(), p.canFly());
                break;
            }
            case "FollowParent": {
                PathfinderFollowParent p = (PathfinderFollowParent) b;
                g = new PathfinderGoalFollowParent((EntityTameableAnimal) m, p.getSpeedModifier());
                break;
            }
            case "JumpOnBlock": {
                PathfinderCatOnBlock p = (PathfinderCatOnBlock) b;
                g = new PathfinderGoalJumpOnBlock((EntityCat) m, p.getSpeedModifier());
                break;
            }
            case "LeapAtTarget": {
                PathfinderLeapAtTarget p = (PathfinderLeapAtTarget) b;
                g = new PathfinderGoalLeapAtTarget(m, p.getHeight());
                break;
            }
            case "LlamaFollow": {
                PathfinderLlamaFollowCaravan p = (PathfinderLlamaFollowCaravan) b;
                g = new PathfinderGoalLlamaFollow((EntityLlama) m, p.getSpeedModifier());
                break;
            }
            case "LookAtPlayer": {
                PathfinderLookAtEntity<?> p = (PathfinderLookAtEntity) b;
                g = new PathfinderGoalLookAtPlayer(m, toNMS(p.getFilter()), p.getRange(), p.getProbability());
                break;
            }
            case "LookAtTradingPlayer": {
                PathfinderLookAtTradingPlayer p = (PathfinderLookAtTradingPlayer) b;
                g = new PathfinderGoalLookAtTradingPlayer((EntityVillagerAbstract) m);
                break;
            }
            case "MeleeAttack": {
                PathfinderMeleeAttack p = (PathfinderMeleeAttack) b;
                g = new PathfinderGoalMeleeAttack((EntityCreature) m, p.getSpeedModifier(), p.mustSee());
                break;
            }
            case "MoveThroughVillage": {
                PathfinderMoveThroughVillage p = (PathfinderMoveThroughVillage) b;
                g = new PathfinderGoalMoveThroughVillage((EntityVillager) m, p.getSpeedModifier(), p.mustBeNight(), p.getMinDistance(), p.canUseDoors());
                break;
            }
            case "MoveTowardsRestriction": {
                PathfinderMoveTowardsRestriction p = (PathfinderMoveTowardsRestriction) b;
                g = new PathfinderGoalMoveTowardsRestriction((EntityCreature) m, p.getSpeedModifier());
                break;
            }
            case "MoveTowardsTarget": {
                PathfinderMoveTowardsTarget p = (PathfinderMoveTowardsTarget) b;
                g = new PathfinderGoalMoveTowardsTarget((EntityCreature) m, p.getSpeedModifier(), p.getRange());
                break;
            }
            case "NearestVillage": {
                PathfinderRandomStrollThroughVillage p = (PathfinderRandomStrollThroughVillage) b;
                g = new PathfinderGoalNearestVillage((EntityCreature) m, p.getInterval());
                break;
            }
            case "OcelotAttack": {
                PathfinderOcelotAttack p = (PathfinderOcelotAttack) b;
                g = new PathfinderGoalOcelotAttack(m);
                break;
            }
            case "OfferFlower": {
                PathfinderOfferFlower p = (PathfinderOfferFlower) b;
                g = new PathfinderGoalOfferFlower((EntityIronGolem) m);
                break;
            }
            case "Panic": {
                PathfinderPanic p = (PathfinderPanic) b;
                g = new PathfinderGoalPanic((EntityCreature) m, p.getSpeedModifier());
                break;
            }
            case "Perch": {
                PathfinderRideShoulder p = (PathfinderRideShoulder) b;
                g = new PathfinderGoalPerch((EntityPerchable) m);
                break;
            }
            case "Raid": {
                PathfinderMoveToRaid p = (PathfinderMoveToRaid) b;
                g = new PathfinderGoalRaid<>((EntityRaider) m);
                break;
            }
            case "RandomFly": {
                PathfinderRandomStrollFlying p = (PathfinderRandomStrollFlying) b;
                g = new PathfinderGoalRandomFly((EntityCreature) m, p.getSpeedModifier());
                break;
            }
            case "RandomLookaround": {
                PathfinderRandomLook p = (PathfinderRandomLook) b;
                g = new PathfinderGoalRandomLookaround(m);
                break;
            }
            case "RandomStroll": {
                PathfinderRandomStroll p = (PathfinderRandomStroll) b;
                g = new PathfinderGoalRandomStroll((EntityCreature) m, p.getSpeedModifier(), p.getInterval());
                break;
            }
            case "RandomStrollLand": {
                PathfinderRandomStrollLand p = (PathfinderRandomStrollLand) b;
                g = new PathfinderGoalRandomStrollLand((EntityCreature) m, p.getSpeedModifier(), p.getProbability());
                break;
            }
            case "RandomSwim": {
                PathfinderRandomSwim p = (PathfinderRandomSwim) b;
                g = new PathfinderGoalRandomSwim((EntityCreature) m, p.getSpeedModifier(), p.getInterval());
                break;
            }
            case "RemoveBlock": {
                PathfinderRemoveBlock p = (PathfinderRemoveBlock) b;
                g = new PathfinderGoalRemoveBlock(((CraftBlock) p.getBlock()).getNMS().getBlock(), (EntityCreature) m, p.getSpeedModifier(), Math.min((int) p.getBlock().getLocation().distance(mob.getLocation()), 1));
                break;
            }
            case "RestrictSun": {
                PathfinderRestrictSun p = (PathfinderRestrictSun) b;
                g = new PathfinderGoalRestrictSun((EntityCreature) m);
                break;
            }
            case "Sit": {
                PathfinderSit p = (PathfinderSit) b;
                g = new PathfinderGoalSit((EntityTameableAnimal) m);
                break;
            }
            case "StrollVillage": {
                PathfinderRandomStrollToVillage p = (PathfinderRandomStrollToVillage) b;
                g = new PathfinderGoalStrollVillage((EntityCreature) m, p.getSpeedModifier(), true);
                break;
            }
            case "StrollVillageGolem": {
                PathfinderRandomStrollInVillage p = (PathfinderRandomStrollInVillage) b;
                g = new PathfinderGoalStrollVillageGolem((EntityCreature) m, p.getSpeedModifier());
                break;
            }
            case "Swell": {
                PathfinderSwellCreeper p = (PathfinderSwellCreeper) b;
                g = new PathfinderGoalSwell((EntityCreeper) m);
                break;
            }
            case "Tame": {
                PathfinderTameHorse p = (PathfinderTameHorse) b;
                g = new PathfinderGoalTame((EntityHorseAbstract) m, p.getSpeedModifier());
                break;
            }
            case "Tempt": {
                PathfinderTempt p = (PathfinderTempt) b;
                g = new PathfinderGoalTempt((EntityCreature) m, p.getSpeedModifier(), RecipeItemStack.a(p.getItems().stream().map(CraftItemStack::asNMSCopy)), true);
                break;
            }
            case "TradeWithPlayer": {
                PathfinderTradePlayer p = (PathfinderTradePlayer) b;
                g = new PathfinderGoalTradeWithPlayer((EntityVillagerAbstract) m);
                break;
            }
            case "UseItem": {
                PathfinderUseItem p = (PathfinderUseItem) b;
                g = new PathfinderGoalUseItem<>(m, toNMS(p.getItem()), toNMS(p.getFinishSound()), e -> p.getCondition().test(fromNMS(e)));
                break;
            }
            case "Water": {
                PathfinderFindWater p = (PathfinderFindWater) b;
                g = new PathfinderGoalWater((EntityCreature) m);
                break;
            }
            case "WaterJump": {
                PathfinderDolphinJump p = (PathfinderDolphinJump) b;
                g = new PathfinderGoalWaterJump((EntityDolphin) m, p.getInterval());
                break;
            }
            case "ZombieAttack": {
                PathfinderZombieAttack p = (PathfinderZombieAttack) b;
                g = new PathfinderGoalZombieAttack((EntityZombie) m, p.getSpeedModifier(), p.mustSee());
                break;
            }
            case "UniversalAngerReset": {
                PathfinderResetAnger p = (PathfinderResetAnger) b;
                g = new PathfinderGoalUniversalAngerReset<>((EntityInsentient & IEntityAngerable) m, p.isAlertingOthers());
                break;
            }

            // Target

            case "DefendVillage": {
                PathfinderDefendVillage p = (PathfinderDefendVillage) b;
                g = new PathfinderGoalDefendVillage((EntityIronGolem) m);
                break;
            }
            case "HurtByTarget": {
                PathfinderHurtByTarget p = (PathfinderHurtByTarget) b;
                List<Class<? extends EntityLiving>> classes = new ArrayList<>();
                p.getIgnoring().stream().map(EntityType::getEntityClass).forEach(c -> classes.add(toNMS(c.asSubclass(LivingEntity.class))));

                g = new PathfinderGoalHurtByTarget((EntityCreature) m, classes.toArray(new Class[0]));
                break;
            }
            case "NearestAttackableTarget": {
                PathfinderNearestAttackableTarget p = (PathfinderNearestAttackableTarget) b;
                g = new PathfinderGoalNearestAttackableTarget<>(m, toNMS(p.getFilter()), p.getInterval(), p.mustSee(), p.mustReach(), t -> p.getCondition().test(fromNMS(t)));
                break;
            }
            case "NearestAttackableTargetWitch": {
                PathfinderNearestAttackableTargetRaider p = (PathfinderNearestAttackableTargetRaider) b;
                g = new PathfinderGoalNearestAttackableTargetWitch<>((EntityRaider) m, toNMS(p.getFilter()), p.getInterval(), p.mustSee(), p.mustReach(), l -> p.getCondition().test(fromNMS(l)));
                break;
            }
            case "NearestHealableRaider": {
                PathfinderNearestHealableRaider p = (PathfinderNearestHealableRaider) b;
                g = new PathfinderGoalNearestHealableRaider<>((EntityRaider) m, toNMS(p.getFilter()), p.mustSee(), l -> p.getCondition().test(fromNMS(l)));
                break;
            }
            case "OwnerHurtByTarget": {
                PathfinderOwnerHurtByTarget p = (PathfinderOwnerHurtByTarget) b;
                g = new PathfinderGoalOwnerHurtByTarget((EntityTameableAnimal) m);
                break;
            }
            case "OwnerHurtTarget": {
                PathfinderOwnerHurtTarget p = (PathfinderOwnerHurtTarget) b;
                g = new PathfinderGoalOwnerHurtTarget((EntityTameableAnimal) m);
                break;
            }

            default: {
                if (b instanceof CustomPathfinder) {
                    CustomPathfinder p = (CustomPathfinder) b;
                    g = new PathfinderGoal() {
                        @Override
                        public boolean a() {
                            return p.canStart();
                        }

                        @Override
                        public boolean b() {
                            return p.canContinueToUse();
                        }

                        @Override
                        public boolean C_() {
                            return p.canInterrupt();
                        }

                        @Override
                        public void c() {
                            p.start();
                        }

                        @Override
                        public void e() {
                            p.tick();
                        }
                    };
                } else g = null;
            }
        }

        return g;
    }

    @Override
    public void addPathfinder(Pathfinder b, int priority, boolean target) {
        Mob mob = b.getEntity();
        EntityInsentient m = toNMS(mob);
        PathfinderGoalSelector s = target ? m.targetSelector : m.goalSelector;

        String name = b.getInternalName().startsWith("PathfinderGoal") ? b.getInternalName().replace("PathfinderGoal", "") : b.getInternalName();
        PathfinderGoal g = toNMS(b);

        if (g == null) return;
        s.a(priority, g);
    }

    @Override
    public void removePathfinder(Pathfinder b, boolean target) {
        Mob mob = b.getEntity();
        EntityInsentient m = toNMS(mob);
        PathfinderGoalSelector s = target ? m.targetSelector : m.goalSelector;

        final PathfinderGoal g = toNMS(b);
        if (g == null) return;
        s.a(g);
    }

    @Override
    public void clearPathfinders(Mob mob, boolean target) {
        EntityInsentient m = toNMS(mob);
        PathfinderGoalSelector s = target ? m.targetSelector : m.goalSelector;

        getGoals(mob, target).forEach(w -> removePathfinder(w.getPathfinder(), target));
    }

    private static BehaviorResult.Status fromNMS(Behavior.Status status) {
        if (status == Behavior.Status.STOPPED) return BehaviorResult.Status.STOPPED;
        return BehaviorResult.Status.RUNNING;
    }

    private static final class BehaviorResult1_16_R3 extends BehaviorResult {
        private final Behavior<? super EntityLiving> b;
        private final EntityInsentient mob;
        private final WorldServer l;

        private BehaviorResult1_16_R3(Behavior<? super EntityLiving> b, EntityInsentient mob) {
            this.b = b;
            this.mob = mob;
            this.l = toNMS(Bukkit.getWorld(mob.world.getWorld().getUID()));

            b.e(l, mob, 0);
        }

        @Override
        public @NotNull Status getStatus() {
            return fromNMS(b.a());
        }

        @Override
        public void stop() {
            b.g(l, mob, 0);
        }
    }

    private static LivingEntity fromNMS(EntityLiving l) {
        return (LivingEntity) l.getBukkitEntity();
    }

    private static MemoryModuleType<?> toNMS(Memory<?> mem) {
        return IRegistry.MEMORY_MODULE_TYPE.get(new MinecraftKey(mem.getKey().getKey()));
    }

    @Override
    public BehaviorResult runBehavior(Mob m, String behaviorName, Object... args) {
        return runBehavior(m, behaviorName, Behavior.class.getPackage().getName(), args);
    }

    @Override
    public BehaviorResult runBehavior(Mob m, String behaviorName, String packageName, Object... args) {
        EntityInsentient nms = toNMS(m);
        String packageN = packageName.replace("{V}", "v1_16_R3");

        for (int i = 0; i < args.length; i++) {
            Object o = args[i];
            if (o instanceof Villager.Profession) args[i] = toNMS((Villager.Profession) o);
            if (o instanceof Memory<?>) args[i] = toNMS((Memory<?>) o);

            if (o instanceof Predicate) args[i] = (Predicate) obj -> {
                if (obj instanceof EntityInsentient) return ((Predicate<Mob>) o).test(fromNMS((EntityInsentient) obj));

                return ((Predicate) o).test(obj);
            };

            if (o instanceof Function) args[i] = (Function) obj -> {
                if (obj instanceof EntityLiving) return ((Function<LivingEntity, ?>) o).apply(fromNMS((EntityLiving) obj));

                return ((Function) o).apply(obj);
            };
        }

        try {
            Class<?> bClass = Class.forName(packageName + "." + behaviorName);
            Constructor<?> c = bClass.getConstructor(Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
            Behavior<? super EntityLiving> b = (Behavior<? super EntityLiving>) c.newInstance(args);
            return new BehaviorResult1_16_R3(b, nms);
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) Bukkit.getLogger().severe(s.toString());
            return null;
        }
    }

    private static EntityPlayer toNMS(Player p) { return ((CraftPlayer) p).getHandle(); }

    private static final class EntityController1_16_R3 implements EntityController {

        private final ControllerJump jumpC;
        private final ControllerMove moveC;
        private final ControllerLook lookC;

        private final Mob m;

        private final EntityInsentient nms;

        public EntityController1_16_R3(Mob m) {
            EntityInsentient nms = toNMS(m);
            this.lookC = nms.getControllerLook();
            this.moveC = nms.getControllerMove();
            this.jumpC = nms.getControllerJump();
            this.m = m;
            this.nms = nms;
        }

        @Override
        public EntityController jump() {
            jumpC.jump();
            jumpC.b();
            return this;
        }

        @Override
        public boolean isLookingAtTarget() {
            Vector dir = m.getLocation().getDirection();
            int x = dir.getBlockX();
            int y = dir.getBlockY();
            int z = dir.getBlockZ();
            return lookC.d() == x && lookC.e() == y && lookC.f() == z;
        }

        @Override
        public EntityController moveTo(double x, double y, double z, double speedMod) {
            moveC.a(x, y, z, speedMod);
            moveC.a();
            nms.getNavigation().a(moveC.d(), moveC.e(), moveC.f(), moveC.c());
            nms.getNavigation().c();
            return this;
        }

        @Override
        public EntityController strafe(float fwd, float right) {
            moveC.a(fwd, right);
            moveC.a();
            nms.getNavigation().a(moveC.d(), moveC.e(), moveC.f(), moveC.c());
            nms.getNavigation().c();
            return this;
        }

        @Override
        public double getCurrentSpeedModifier() {
            return moveC.c();
        }

        @Override
        public Location getTargetMoveLocation() {
            return new Location(m.getWorld(), moveC.d(), moveC.e(), moveC.f());
        }

        @Override
        public Location getTargetLookLocation() {
            return new Location(m.getWorld(), lookC.d(), lookC.e(), lookC.f());
        }

        @Override
        public EntityController lookAt(double x, double y, double z) {
            lookC.a(x, y, z);
            lookC.a();
            return this;
        }

    }

    private static final class NavigationPath1_16_R3 implements NavigationPath {
        private String name;
        private final Mob m;
        private final PathEntity handle;

        NavigationPath1_16_R3(@NotNull PathEntity nms, @NotNull Mob m) {
            this.m = m;
            this.name = "bukkitpath";
            this.handle = nms;
        }

        private final List<Position> nodes = new ArrayList<>();

        /**
         * Advances this path.
         */
        @Override
        public void advance() {
            this.handle.a();
            PathPoint n = handle.h();
            new EntityController1_16_R3(m).moveTo(n.a, n.b, n.c);
        }

        /**
         * Get this Path's Name.
         * @return this path's name
         */
        public String getName() {
            return this.name;
        }

        /**
         * Sets this Path's Name.
         * @param name this path's new name
         */
        public void setName(@NotNull String name) {
            this.name = name;
        }

        /**
         * Whether this NavigationPath is complete.
         * @return true if complete, else false
         */
        @Override
        public boolean isDone() {
            return this.handle.c();
        }

        /**
         * Get the size of this NavigationPath.
         * @return size
         */
        public int size() {
            return nodes.size();
        }

        /**
         * Whether this NavigationPath is empty.
         * @return true if empty, else false
         */
        @Override
        public boolean isEmpty() {
            return nodes.isEmpty();
        }

        /**
         * Whether this Path contains this Navigation Node.
         * @param o Position
         * @return true if contains, else false
         */
        @Override
        public boolean contains(@Nullable Position o) {
            return nodes.contains(o);
        }

        @Override
        @NotNull
        public Iterator<Position> iterator() {
            return nodes.iterator();
        }

        /**
         * Converts this NavigationPath into an Array of Nodes.
         * @return Array of Position
         */
        @NotNull
        @Override
        public Position[] toArray() {
            return nodes.toArray(new Position[0]);
        }

        /**
         * Returns the index of this Navigation Node.
         * @param o Position to fetch
         * @return Index found
         * @see List#indexOf(Object)
         */
        @Override
        public int indexOf(@Nullable Position o) {
            return nodes.indexOf(o);
        }

        /**
         * Returns the last index of this Navigation Node.
         * @param o Position to fetch
         * @return Index found
         * @see List#lastIndexOf(Object)
         */
        @Override
        public int lastIndexOf(@Nullable Position o) {
            return nodes.lastIndexOf(o);
        }
    }

    private static final class EntityNavigation1_16_R3 implements EntityNavigation {

        private final NavigationAbstract handle;

        private int speedMod;
        private int range;
        private final List<Position> points;
        private BlockPosition finalPos;

        private final Mob m;

        EntityNavigation1_16_R3(Mob m) {
            this.handle = toNMS(m).getNavigation();
            this.points = new ArrayList<>();

            this.speedMod = 1;
            this.range = Integer.MAX_VALUE;
            this.m = m;
        }

        @Override
        public double getSpeedModifier() {
            return this.speedMod;
        }

        @Override
        public void setSpeedModifier(double mod) throws IllegalArgumentException {
            if (mod > Integer.MAX_VALUE) throw new IllegalArgumentException("Must be integer");
            this.speedMod = (int) Math.floor(mod);
        }

        @Override
        public EntityNavigation recompute() {
            this.handle.j();
            return this;
        }

        @Override
        public EntityNavigation addPoint(@NotNull Position point) {
            this.points.add(point);
            return this;
        }

        @Override
        public EntityNavigation addPoint(int index, @NotNull Position point) {
            this.points.add(index, point);
            return this;
        }

        @Override
        public EntityNavigation removePoint(@NotNull Position point) {
            this.points.remove(point);
            return this;
        }

        @Override
        public EntityNavigation removePoint(int index) {
            this.points.remove(index);
            return this;
        }

        @Override
        @NotNull
        public NavigationPath buildPath() {
            return new NavigationPath1_16_R3(handle.a(finalPos, range), m);
        }

        @Override
        public EntityNavigation setFinalPoint(@NotNull Position node) {
            this.finalPos = new BlockPosition(node.getX(), node.getY(), node.getZ());
            return this;
        }

        @Override
        public EntityNavigation setRange(int range) {
            this.range = range;
            return this;
        }
    }

    private static final class EntityBody1_16_R3 implements EntityBody {
        private final EntityInsentient nmsMob;

        EntityBody1_16_R3(Mob nmsMob) {
            this.nmsMob = toNMS(nmsMob);
        }

        /**
         * Whether this Entity is Left Handed.
         *
         * @return true if left-handed, else false
         */
        @Override
        public boolean isLeftHanded() {
            return nmsMob.isLeftHanded();
        }

        /**
         * Sets this Entity to be left-handed.
         *
         * @param leftHanded true if left-handed, else false
         */
        @Override
        public void setLeftHanded(boolean leftHanded) {
            nmsMob.setLeftHanded(leftHanded);
        }

        @Override
        public boolean canBreatheUnderwater() {
            return nmsMob.cM();
        }

        @Override
        public boolean shouldDiscardFriction() {
            return false;
        }

        @Override
        public void setDiscardFriction(boolean discard) {
            // doesn't exist
        }

        /**
         * Makes this Mob interact with a Player.
         *
         * @param p Player to interact with
         * @param hand Hand to use
         * @return Result of interaction
         */
        @Override
        public InteractionResult interact(@NotNull Player p, @Nullable InteractionHand hand) {
            final EnumHand h;

            if (hand == InteractionHand.OFF_HAND) h = EnumHand.OFF_HAND;
            else h = EnumHand.MAIN_HAND;

           switch (nmsMob.a(toNMS(p), h)) {
                case SUCCESS: return InteractionResult.SUCCESS;
                case CONSUME: return InteractionResult.CONSUME;
                case FAIL: return InteractionResult.FAIL;
                default: return InteractionResult.PASS;
            }
        }

        @Override
        public boolean isSensitiveToWater() {
            return nmsMob.dO();
        }

        @Override
        public boolean isAffectedByPotions() {
            return nmsMob.eh();
        }

        @Override
        public boolean isBlocking() {
            return nmsMob.isBlocking();
        }

        @Override
        public float getArmorCoverPercentage() {
            return nmsMob.dF();
        }

        @Override
        public void useItem(@Nullable InteractionHand hand) {
            if (hand == null) return;

            final EnumHand h;
            if (hand == InteractionHand.OFF_HAND) h = EnumHand.OFF_HAND;
            else h = EnumHand.MAIN_HAND;

            nmsMob.c(h);
        }

        @Override
        public boolean isUsingItem() {
            return nmsMob.isHandRaised();
        }

        @Override
        public boolean isFireImmune() {
            return nmsMob.isFireProof();
        }

        @Override
        public boolean isSwinging() {
            return nmsMob.ai;
        }

        @Override
        public boolean canRideUnderwater() {
            return nmsMob.bt();
        }

        @Override
        public boolean isInvisibleTo(@Nullable Player p) {
            return false;
        }

        @Override
        public @NotNull InteractionHand getMainHand() {
            if (nmsMob.getMainHand() == EnumMainHand.LEFT) return InteractionHand.OFF_HAND;
            return InteractionHand.MAIN_HAND;
        }

        @Override
        public List<ItemStack> getDefaultDrops() {
            try {
                Field dropsF = EntityLiving.class.getDeclaredField("drops");
                dropsF.setAccessible(true);
                List<ItemStack> drops = (List<ItemStack>) dropsF.get(nmsMob);
                return new ArrayList<>(drops);
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        @Override
        public void setDefaultDrops(@Nullable ItemStack... drops) {
            try {
                Field dropsF = EntityLiving.class.getDeclaredField("drops");
                dropsF.setAccessible(true);
                dropsF.set(nmsMob, drops == null ? new ArrayList<>() : Arrays.asList(drops));
            } catch (Exception ignored) {}
        }

        @Override
        public boolean isInCombat() {
            try {
                Field inCombat = nmsMob.combatTracker.getClass().getDeclaredField("f");
                inCombat.setAccessible(true);
                return inCombat.getBoolean(nmsMob.combatTracker);
            } catch (Exception ignored) {}
            return false;
        }

        @Override
        public float getFlyingSpeed() {
            return nmsMob.aE;
        }

        @Override
        public void setFlyingSpeed(float speed) throws IllegalArgumentException {
            if (speed < 0 || speed > 1) throw new IllegalArgumentException("Flying speed must be between 0.0F and 1.0F");
            nmsMob.aE = speed;
        }

        @Override
        public boolean isForcingDrops() {
            try {
                Field forceDrops = EntityLiving.class.getDeclaredField("forceDrops");
                forceDrops.setAccessible(true);
                return forceDrops.getBoolean(nmsMob);
            } catch (Exception ignored) {}
            return false;
        }

        @Override
        public void setForcingDrops(boolean drop) {
            try {
                Field forceDrops = EntityLiving.class.getDeclaredField("forceDrops");
                forceDrops.setAccessible(true);
                forceDrops.set(nmsMob, drop);
            } catch (Exception ignored) {}
        }

        @Override
        public boolean isMoving() {
            return false; // doesn't exist
        }

        @Override
        public float getBodyRotation() {
            return nmsMob.aA;
        }

        @Override
        public void setBodyRotation(float rotation) {
            nmsMob.aA = rotation > 360 ? (rotation - (float) (360 * Math.floor(rotation / 360))) : rotation;
        }

        @Override
        public float getHeadRotation() {
            return nmsMob.aC;
        }

        @Override
        public void setHeadRotation(float rotation) {
            nmsMob.aC = rotation > 360 ? (rotation - (float) (360 * Math.floor(rotation / 360))) : rotation;
        }

        @Override
        public Set<? extends Entity> getCollideExemptions() {
            return nmsMob.collidableExemptions.stream().map(Bukkit::getEntity).filter(Objects::nonNull).collect(Collectors.toSet());
        }

        @Override
        public void addCollideExemption(@NotNull Entity en) throws IllegalArgumentException {
            if (en == null) throw new IllegalArgumentException("Entity cannot be null");
            nmsMob.collidableExemptions.add(en.getUniqueId());
        }

        @Override
        public void removeCollideExemption(@NotNull Entity en) throws IllegalArgumentException {
            if (en == null) throw new IllegalArgumentException("Entity cannot be null");
            nmsMob.collidableExemptions.remove(en.getUniqueId());
        }

        @Override
        public int getDroppedExperience() {
            return nmsMob.expToDrop;
        }

        @Override
        public void setDroppedExperience(int exp) throws IllegalArgumentException {
            if (exp < 0) throw new IllegalArgumentException("Experience cannot be negative");
            nmsMob.expToDrop = exp;
        }

        @Override
        public void playAnimation(@NotNull EntityAnimation anim) {
            switch (anim) {
                case DAMAGE: {
                    nmsMob.hurtDuration = 10;
                    nmsMob.hurtTicks = nmsMob.hurtDuration;
                    nmsMob.ap = 0.0F;
                    break;
                }
                case CRITICAL_DAMAGE: {
                    PacketPlayOutAnimation pkt = new PacketPlayOutAnimation(nmsMob, 4);
                    for (Player p : fromNMS(nmsMob).getWorld().getPlayers()) toNMS(p).playerConnection.sendPacket(pkt);
                    break;
                }
                case MAGICAL_CRITICAL_DAMAGE: {
                    PacketPlayOutAnimation pkt = new PacketPlayOutAnimation(nmsMob, 5);
                    for (Player p : fromNMS(nmsMob).getWorld().getPlayers()) toNMS(p).playerConnection.sendPacket(pkt);
                    break;
                }
            }
        }

        @Override
        public float getAnimationSpeed() {
            return nmsMob.av;
        }

        @Override
        public void setAnimationSpeed(float speed) throws IllegalArgumentException {
            if (speed < 0) throw new IllegalArgumentException("Animation speed cannot be negative");
            nmsMob.av = speed;
        }

        @Override
        public boolean hasVerticalCollision() {
            return nmsMob.v;
        }

        @Override
        public void setVerticalCollision(boolean collision) {
            nmsMob.v = collision;
        }

        @Override
        public boolean hasHorizontalCollision() {
            return nmsMob.positionChanged;
        }

        @Override
        public void setHorizontalCollision(boolean collision) {
            nmsMob.positionChanged = collision;
        }

        @Override
        public float getWalkDistance() {
            return nmsMob.A;
        }

        @Override
        public float getMoveDistance() {
            return nmsMob.B;
        }

        @Override
        public float getFlyDistance() {
            return 0F; // doesn't exist
        }

        @Override
        public boolean isImmuneToExplosions() {
            return nmsMob.ci();
        }

        @Override
        public boolean isPeacefulCompatible() {
            try {
                Method m = EntityInsentient.class.getDeclaredMethod("L");
                m.setAccessible(true);
                return (boolean) m.invoke(nmsMob);
            } catch (Exception e) {
                return false;
            }
        }
    }

    @Override
    public Attribute getDefaultAttribute(String s) {
        return new Attribute1_16_R3((AttributeRanged) IRegistry.ATTRIBUTE.get(new MinecraftKey(s)));
    }

    private static Activity toNMS(me.gamercoder215.mobchip.ai.schedule.Activity a) {
        return IRegistry.ACTIVITY.get(new MinecraftKey(a.getKey().getKey()));
    }

    private static me.gamercoder215.mobchip.ai.schedule.Activity fromNMS(Activity a) {
        MinecraftKey key = IRegistry.ACTIVITY.getKey(a);
        return me.gamercoder215.mobchip.ai.schedule.Activity.getByKey(NamespacedKey.minecraft(key.getKey()));
    }

    private static me.gamercoder215.mobchip.ai.schedule.Schedule fromNMS(Schedule s) {
        me.gamercoder215.mobchip.ai.schedule.Schedule.Builder b = me.gamercoder215.mobchip.ai.schedule.Schedule.builder();
        for (int i = 0; i < 24000; i++) {
            if (s.a(i) == null) continue;
            me.gamercoder215.mobchip.ai.schedule.Activity a = fromNMS(s.a(i));
            b.addActivity(i, a);
        }

        return b.build();
    }

    private static Schedule toNMS(me.gamercoder215.mobchip.ai.schedule.Schedule s) {
        ScheduleBuilder b = new ScheduleBuilder(new Schedule());
        for (int i = 0; i < 24000; i++) {
            if (!s.contains(i)) continue;
            Activity a = toNMS(s.get(i));
            b.a(i, a);
        }

        return b.a();
    }

    private static <T extends EntityLiving> Behavior<T> toNMS(Consumer<Mob> en) {
        return new Behavior<T>(Collections.emptyMap()) {
            @Override
            protected void d(WorldServer var0, T m, long var2) {
                if (!(m instanceof EntityInsentient)) return;
                en.accept(fromNMS((EntityInsentient) m));
            }
        };
    }

    @Override
    public me.gamercoder215.mobchip.ai.schedule.Schedule getDefaultSchedule(String key) {
        return fromNMS(IRegistry.SCHEDULE.get(new MinecraftKey(key)));
    }

    private static Set<me.gamercoder215.mobchip.ai.schedule.Activity> getActiveActivities(Mob m) {
        EntityInsentient nmsMob = toNMS(m);

        try {
            Field active = nmsMob.getBehaviorController().getClass().getDeclaredField("j");
            active.setAccessible(true);
            Set<Activity> activities = (Set<Activity>) active.get(nmsMob.getBehaviorController());
            return activities.stream().map(ChipUtil1_16_R3::fromNMS).collect(Collectors.toSet());
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getClass().getSimpleName());
            Bukkit.getLogger().severe(e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) Bukkit.getLogger().severe(s.toString());
        }

        return Collections.emptySet();
    }

    @SuppressWarnings("deprecation")
    private static final class EntityScheduleManager1_16_R3 implements EntityScheduleManager {

        private final EntityInsentient nmsMob;
        private final Mob m;

        EntityScheduleManager1_16_R3(Mob m) {
            this.m = m;
            this.nmsMob = toNMS(m);
        }


        @Override
        public @Nullable me.gamercoder215.mobchip.ai.schedule.Schedule getCurrentSchedule() {
            return fromNMS(nmsMob.getBehaviorController().getSchedule());
        }

        @Override
        public void setSchedule(@NotNull me.gamercoder215.mobchip.ai.schedule.Schedule s) {
            nmsMob.getBehaviorController().setSchedule(toNMS(s));
        }

        @Override
        public @NotNull Set<me.gamercoder215.mobchip.ai.schedule.Activity> getActiveActivities() {
            return ChipUtil1_16_R3.getActiveActivities(m);
        }

        @Override
        public void setDefaultActivity(@NotNull me.gamercoder215.mobchip.ai.schedule.Activity a) {
            nmsMob.getBehaviorController().b(toNMS(a));
        }

        @Override
        public void useDefaultActivity() {
            nmsMob.getBehaviorController().e();
        }

        @Override
        public void setRunningActivity(@NotNull me.gamercoder215.mobchip.ai.schedule.Activity a) {
            nmsMob.getBehaviorController().a(toNMS(a));
        }

        @Override
        public @Nullable me.gamercoder215.mobchip.ai.schedule.Activity getRunningActivity() {
            return nmsMob.getBehaviorController().f().isPresent() ? fromNMS(nmsMob.getBehaviorController().f().get()) : null;
        }

        @Override
        public boolean isRunning(@NotNull me.gamercoder215.mobchip.ai.schedule.Activity a) {
            return nmsMob.getBehaviorController().c(toNMS(a));
        }

        @Override
        public int size() {
            return nmsMob.getBehaviorController().d().size();
        }

        @Override
        public boolean isEmpty() {
            return nmsMob.getBehaviorController().d().isEmpty();
        }

        @Nullable
        @Override
        public Consumer<Mob> put(@NotNull me.gamercoder215.mobchip.ai.schedule.Activity key, Consumer<Mob> value) {
            nmsMob.getBehaviorController().a(toNMS(key), 0, ImmutableList.of(toNMS(value)));
            return value;
        }

        @Override
        public void clear() {
            // doesn't exist
        }

    }

    @Override
    public EntityScheduleManager getManager(Mob m) { return new EntityScheduleManager1_16_R3(m); }

    @Override
    public EntityController getController(Mob m) {
        return new EntityController1_16_R3(m);
    }

    @Override
    public EntityNavigation getNavigation(Mob m) {
        return new EntityNavigation1_16_R3(m);
    }

    @Override
    public EntityBody getBody(Mob m) {
        return new EntityBody1_16_R3(m);
    }

    private static DamageSource toNMS(EntityDamageEvent.DamageCause c) {
        switch (c) {
            case FIRE:
            case FIRE_TICK: return DamageSource.FIRE;
            case LIGHTNING: return DamageSource.LIGHTNING;
            case SUFFOCATION: return DamageSource.STUCK;
            case LAVA: return DamageSource.LAVA;
            case HOT_FLOOR: return DamageSource.HOT_FLOOR;
            case CRAMMING: return DamageSource.CRAMMING;
            case DROWNING: return DamageSource.DROWN;
            case STARVATION: return DamageSource.STARVE;
            case CONTACT: return DamageSource.CACTUS;
            case MAGIC: return DamageSource.MAGIC;
            case FALL: return DamageSource.FALL;
            case FLY_INTO_WALL: return DamageSource.FLY_INTO_WALL;
            case VOID: return DamageSource.OUT_OF_WORLD;
            case WITHER: return DamageSource.WITHER;
            case FALLING_BLOCK: return DamageSource.FALLING_BLOCK;
            case DRAGON_BREATH: return DamageSource.DRAGON_BREATH;
            case DRYOUT: return DamageSource.DRYOUT;
            default: return DamageSource.GENERIC;
        }
    }

    private static AbstractDragonController toNMS(CustomPhase c) {
        return new AbstractDragonController(((CraftEnderDragon) c.getDragon()).getHandle()) {
            @Override
            public DragonControllerPhase<? extends IDragonController> getControllerPhase() {
                try {
                    Method create = DragonControllerPhase.class.getDeclaredMethod("a");
                    create.setAccessible(true);
                    return (DragonControllerPhase<? extends IDragonController>) create.invoke(null, this.getClass(), c.getKey().getKey());
                } catch (Exception ignored) {}
                return DragonControllerPhase.HOVER;
            }

            public void d() { c.start(); }
            public void e() { c.stop(); }
            public boolean a() { return c.isSitting(); }
            public void b() { c.clientTick(); }
            public void c() { c.serverTick(); }
            public void a(EntityEnderCrystal crystal, BlockPosition pos, DamageSource s, EntityHuman p) {
                EnderCrystal bCrystal = (EnderCrystal) crystal.getBukkitEntity();
                c.onCrystalDestroyed(bCrystal, fromNMS(s), p == null ? null : Bukkit.getPlayer(p.getUniqueID()));
            }
            public Vec3D g() {
                Location l = c.getTargetLocation();
                return new Vec3D(l.getX(), l.getY(), l.getZ());
            }
            public float f() { return c.getFlyingSpeed(); }
            public float a(DamageSource s, float damage) { return c.onDamage(fromNMS(s), damage); }
        };
    }

    @Override
    public void setCustomPhase(EnderDragon a, CustomPhase c) {
        EntityEnderDragon nmsMob = ((CraftEnderDragon) a).getHandle();
        AbstractDragonController nmsPhase = toNMS(c);
        try {
            new DragonControllerManager(nmsMob).setControllerPhase(nmsPhase.getControllerPhase());
        } catch (IndexOutOfBoundsException ignored) {}
    }

    private static EntityItem toNMS(org.bukkit.entity.Item i) {
        return (EntityItem) ((CraftItem) i).getHandle();
    }

    private static EntityLiving toNMS(LivingEntity en) {
        return ((CraftLivingEntity) en).getHandle();
    }

    private static Object toNMS(String key, Object value) {
        final Object nmsValue;

        if (value instanceof Location) {
            Location l = (Location) value;
            if (key.equals("nearest_bed") || key.equals("celebrate_location") || key.equals("nearest_repellent")) nmsValue = new BlockPosition(l.getX(), l.getY(), l.getZ());
            else nmsValue = GlobalPos.create(toNMS(l.getWorld()).getDimensionKey(), new BlockPosition(l.getX(), l.getY(), l.getZ()));
        }
        else if (value instanceof Location[]) {
            Location[] ls = (Location[]) value;
            List<GlobalPos> p = new ArrayList<>();

            for (Location l : ls) {
                p.add(GlobalPos.create(toNMS(l.getWorld()).getDimensionKey(), new BlockPosition(l.getX(), l.getY(), l.getZ())));
            }

            nmsValue = p;
        }
        else if (value instanceof Player) {
            Player p = (Player) value;
            if (key.equals("liked_player")) nmsValue = p.getUniqueId();
            else nmsValue = toNMS(p);
        }
        else if (value instanceof Memory.WalkingTarget) {
            Memory.WalkingTarget t = (Memory.WalkingTarget) value;
            nmsValue = new MemoryTarget(toNMS(t.getLocation()), (float) t.getSpeedModifier(), t.getDistance());
        }
        else if (value instanceof LivingEntity){
            LivingEntity l = (LivingEntity) value;
            nmsValue = toNMS(l);
        }
        else if (value instanceof Entity) {
            Entity e = (Entity) value;
            if (key.equals("angry_at")) nmsValue = e.getUniqueId();
            else nmsValue = toNMS(e);
        }
        else if (value instanceof org.bukkit.block.Block[]) {
            org.bukkit.block.Block[] b = (org.bukkit.block.Block[]) value;
            final Collection<GlobalPos> s;
            if (key.equals("doors_to_close")) s = new HashSet<>();
            else s = new ArrayList<>();

            for (org.bukkit.block.Block bl : b) {
                Location l = bl.getLocation();
                s.add(GlobalPos.create(toNMS(l.getWorld()).getDimensionKey(), new BlockPosition(l.getX(), l.getY(), l.getZ())));
            }
            nmsValue = s;
        }
        else if (value instanceof Villager[]) {
            Villager[] vs = (Villager[]) value;
            List<EntityLiving> s = new ArrayList<>();
            for (Villager v : vs) s.add(toNMS(v));
            nmsValue = s;
        }
        else if (value instanceof Player[]) {
            Player[] ps = (Player[]) value;
            List<EntityPlayer> s = new ArrayList<>();
            for (Player p : ps) s.add(toNMS(p));
            nmsValue = s;
        }
        else if (value instanceof LivingEntity[]) {
            LivingEntity[] ls = (LivingEntity[]) value;
            List<EntityLiving> s = new ArrayList<>();
            for (LivingEntity l : ls) s.add(toNMS(l));
            nmsValue = s;
        }
        else if (value instanceof EntityDamageEvent.DamageCause) {
            EntityDamageEvent.DamageCause c = (EntityDamageEvent.DamageCause) value;
            nmsValue = toNMS(c);
        }
        else nmsValue = value;

        return nmsValue;
    }

    private static Object fromNMS(Mob m, String key, Object nmsValue) {
        Object value = nmsValue;

        if (nmsValue instanceof GlobalPos) {
            GlobalPos l = (GlobalPos) nmsValue;
            BlockPosition pos = l.getBlockPosition();
            World w = ((CraftServer) Bukkit.getServer()).getHandle().getServer().customRegistry.b(IRegistry.L).a(l.getDimensionManager()).getWorld();
            value = new Location(w, pos.getX(), pos.getY(), pos.getZ());
        }
        else if (nmsValue instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer) nmsValue;
            value = Bukkit.getPlayer(p.getUniqueID());
        }
        else if (nmsValue instanceof MemoryTarget) {
            MemoryTarget t = (MemoryTarget) nmsValue;
            BlockPosition p = t.a().b();
            value = new Memory.WalkingTarget(new Location(m.getWorld(), p.getX(), p.getY(), p.getZ()), t.b(), t.c());
        }
        else if (nmsValue instanceof EntityLiving) {
            EntityLiving l = (EntityLiving) nmsValue;
            value = Bukkit.getEntity(l.getUniqueID());
        }
        else if (nmsValue instanceof Set<?>) {
            Set<?> s = (Set<?>) nmsValue;
            if (key.equals("doors_to_close")) {
                List<org.bukkit.block.Block> l = new ArrayList<>();
                s.forEach(o -> l.add((org.bukkit.block.Block) fromNMS(m, key, o)));
                value = l.toArray(new org.bukkit.block.Block[0]);
            }
        }
        else if (nmsValue instanceof List<?>) {
            List<?> ls = (List<?>) nmsValue;
            switch (key) {
                case "visible_villager_babies": {
                    List<Villager> vl = new ArrayList<>();
                    ls.forEach(o -> vl.add((Villager) fromNMS((EntityLiving) o)));
                    value = vl.toArray(new Villager[0]);
                    break;
                }
                case "nearest_players": {
                    List<Player> pl = new ArrayList<>();
                    ls.forEach(o -> pl.add(Bukkit.getPlayer(((EntityPlayer) o).getUniqueID())));
                    value = pl.toArray(new Player[0]);
                    break;
                }
                case "mobs": {
                    List<LivingEntity> vl = new ArrayList<>();
                    ls.forEach(o -> vl.add(fromNMS((EntityLiving) o)));
                    value = vl.toArray(new LivingEntity[0]);
                    break;
                }
                case "secondary_job_site":
                case "interactable_doors": {
                    List<Location> l = new ArrayList<>();
                    ls.forEach(o -> l.add((Location) fromNMS(m, key, o)));
                    value = l.toArray(new Location[0]);
                    break;
                }
            }
        }
        else if (value instanceof DamageSource) {
            DamageSource c = (DamageSource) value;
            value = fromNMS(c);
        }
        else value = nmsValue;

        return value;
    }

    private static EntityDamageEvent.DamageCause fromNMS(DamageSource c) {
        switch (c.translationIndex) {
            case "inFire": return FIRE;
            case "lightningBolt": return LIGHTNING;
            case "onFire": return FIRE_TICK;
            case "lava": return LAVA;
            case "hotFloor": return HOT_FLOOR;
            case "inWall": return SUFFOCATION;
            case "cramming": return CRAMMING;
            case "drown": return DROWNING;
            case "starve": return STARVATION;
            case "cactus":
            case "sweetBerryBush": return CONTACT;
            case "fall": return FALL;
            case "flyIntoWall": return FLY_INTO_WALL;
            case "outOfWorld": return VOID;
            case "magic": return MAGIC;
            case "wither": return WITHER;
            case "anvil": case "fallingBlock": return FALLING_BLOCK;
            case "dragonBreath": return DRAGON_BREATH;
            case "dryout": return DRYOUT;
            default: return CUSTOM;
        }
    }


    @Override
    public <T> void setMemory(Mob mob, Memory<T> m, T value) {
        if (value == null) {
            removeMemory(mob, m);
            return;
        }

        EntityInsentient nms = toNMS(mob);
        MemoryModuleType type = toNMS(m);
        String key = IRegistry.MEMORY_MODULE_TYPE.getKey(type).getKey();
        Object nmsValue = toNMS(key, value);

        nms.getBehaviorController().setMemory(type, nmsValue);
    }

    @Override
    public <T> void setMemory(Mob mob, Memory<T> m, T value, long durationTicks) {
        if (value == null) {
            removeMemory(mob, m);
            return;
        }

        EntityInsentient nms = toNMS(mob);
        MemoryModuleType type = toNMS(m);
        String key = IRegistry.MEMORY_MODULE_TYPE.getKey(type).getKey();
        Object nmsValue = toNMS(key, value);

        nms.getBehaviorController().a(type, nmsValue, durationTicks);
    }

    @Override
    public <T> T getMemory(Mob mob, Memory<T> m) {
        EntityInsentient nms = toNMS(mob);
        MemoryModuleType type = toNMS(m);
        String key = IRegistry.MEMORY_MODULE_TYPE.getKey(type).getKey();

        return m.getBukkitClass().cast(fromNMS(mob, key, nms.getBehaviorController().getMemory(type)));
    }

    @Override
    public long getExpiry(Mob mob, Memory<?> m) {
        // doesn't exist
        return 0;
    }

    @Override
    public boolean contains(Mob mob, Memory<?> m) {
        EntityInsentient nms = toNMS(mob);
        MemoryModuleType type = toNMS(m);

        return nms.getBehaviorController().hasMemory(type);
    }

    @Override
    public void removeMemory(Mob mob, Memory<?> m) {
        EntityInsentient nms = toNMS(mob);
        MemoryModuleType<?> type = toNMS(m);
        nms.getBehaviorController().removeMemory(type);
    }

    @Override
    public boolean isRestricted(Mob m) {
        EntityInsentient nms = toNMS(m);
        return nms.ev();
    }

    @Override
    public void clearRestriction(Mob m) {
        EntityInsentient nms = toNMS(m);
        nms.a((BlockPosition) null, 0);
    }

    @Override
    public void restrictTo(Mob m, double x, double y, double z, int radius) {
        EntityInsentient nms = toNMS(m);
        nms.a(new BlockPosition(x, y, z), radius);
    }

    @Override
    public Location getRestriction(Mob m) {
        EntityInsentient nms = toNMS(m);
        BlockPosition c = nms.ew();
        return new Location(m.getWorld(), c.getX(), c.getY(), c.getZ());
    }

    @Override
    public int getRestrictionRadius(Mob m) {
        EntityInsentient nms = toNMS(m);
        return ((int) nms.ex()) < 0 ? Integer.MAX_VALUE : (int) nms.ex();
    }

    @Override
    public boolean hasRestriction(Mob m) {
        EntityInsentient nms = toNMS(m);
        return nms.ez();
    }

    @Override
    public boolean canSee(Mob m, Entity en) {
        EntityInsentient nms = toNMS(m);
        return nms.getEntitySenses().a(toNMS(en));
    }

    private static net.minecraft.server.v1_16_R3.Entity toNMS(Entity en) {
        return ((CraftEntity) en).getHandle();
    }

    private static VillagerProfession toNMS(Villager.Profession p) {
        switch (p) {
            case FARMER: return VillagerProfession.FARMER;
            case FISHERMAN: return VillagerProfession.FISHERMAN;
            case LIBRARIAN: return VillagerProfession.LIBRARIAN;
            case WEAPONSMITH: return VillagerProfession.WEAPONSMITH;
            case TOOLSMITH: return VillagerProfession.TOOLSMITH;
            case BUTCHER: return VillagerProfession.BUTCHER;
            case FLETCHER: return VillagerProfession.FLETCHER;
            case MASON: return VillagerProfession.MASON;
            case CLERIC: return VillagerProfession.CLERIC;
            case ARMORER: return VillagerProfession.ARMORER;
            case NITWIT: return VillagerProfession.NITWIT;
            case SHEPHERD: return VillagerProfession.SHEPHERD;
            case CARTOGRAPHER: return VillagerProfession.CARTOGRAPHER;
            case LEATHERWORKER: return VillagerProfession.LEATHERWORKER;
            default: return VillagerProfession.NONE;
        }
    }

    private static <T extends Entity> Class<? extends T> fromNMS(Class<? extends net.minecraft.server.v1_16_R3.Entity> clazz, Class<T> cast) {
        try {
            String name = clazz.getSimpleName();
            if (name.contains("Entity")) name = name.replace("Entity", "");

            final Class<? extends Entity> bukkit;
            
            switch (name) {
                case "": bukkit = Entity.class; break;
                case "Living": bukkit = LivingEntity.class; break;
                case "Lightning": bukkit = LightningStrike.class; break;
                case "Insentient": bukkit = Mob.class; break;
                case "TameableAnimal": bukkit = Tameable.class; break;

                case "Animal": bukkit = Animals.class; break;
                case "FishSchool": bukkit = Fish.class; break;
                case "HorseAbstract": bukkit = AbstractHorse.class; break;
                case "HorseMule": bukkit = Mule.class; break;
                case "HorseSkeleton": bukkit = SkeletonHorse.class; break;
                case "HorseZombie": bukkit = ZombieHorse.class; break;
                case "HorseDonkey": bukkit = Donkey.class; break;
                case "WaterAnimal": bukkit = WaterMob.class; break;

                case "GiantZombie": bukkit = Giant.class; break;
                case "GuardianElder": bukkit = ElderGuardian.class; break;
                case "IllagerIllusioner": bukkit = Illusioner.class; break;
                case "SkeletonStray": bukkit = Stray.class; break;
                case "SkeletonWither": bukkit = WitherSkeleton.class; break;
                case "ZombieHusk": bukkit = Husk.class; break;
                case "ZombieVillager": bukkit = ZombieVillager.class; break;

                case "Villager": bukkit = Villager.class; break;
                case "VillagerAbstract": bukkit = AbstractVillager.class; break;
                case "VillagerTrader": bukkit = WanderingTrader.class; break;

                case "Human": bukkit = HumanEntity.class; break;
                case "Player": bukkit = Player.class; break;

                case "FireballFireball": bukkit = SizedFireball.class; break;
                case "Fireworks": bukkit = Firework.class; break;
                case "FishingHook": bukkit = FishHook.class; break;
                case "Potion": bukkit = ThrownPotion.class; break;
                case "ProjectileThrowable": bukkit = ThrowableProjectile.class; break;
                case "ThrownTrident": bukkit = Trident.class; break;

                case "MinecartAbstract": bukkit = Minecart.class; break;
                case "MinecartChest": bukkit = StorageMinecart.class; break;
                case "MinecartCommandBlock": bukkit = CommandMinecart.class; break;
                case "MinecartFurnace": bukkit = PoweredMinecart.class; break;
                case "MinecartHopper": bukkit = HopperMinecart.class; break;
                case "MinecartMobSpawner": bukkit = SpawnerMinecart.class; break;
                case "MinecartTNT": bukkit = ExplosiveMinecart.class; break;

                default: bukkit = Class.forName("org.bukkit.entity." + name).asSubclass(Entity.class);
            }

            return bukkit.asSubclass(cast);
        } catch (ClassNotFoundException e) {
            return cast;
        }
    }

    private static EntityInsentient toNMS(Mob m) { return ((CraftMob) m).getHandle(); }

    private static EntityType[] getEntityTypes(Class<?>... nms) {
        List<EntityType> types = new ArrayList<>();
        for (Class<?> c : nms) {

            Class<? extends Entity> bukkit = fromNMS((Class<? extends net.minecraft.server.v1_16_R3.Entity>) c, Entity.class);
            for (EntityType t : EntityType.values()) if (t.getEntityClass().isAssignableFrom(bukkit)) types.add(t);
        }
        return types.toArray(new EntityType[0]);
    }

    private static EnumDifficulty toNMS(org.bukkit.Difficulty d) {
        switch (d) {
            case PEACEFUL: return EnumDifficulty.PEACEFUL;
            default: return EnumDifficulty.EASY;
            case NORMAL: return EnumDifficulty.NORMAL;
            case HARD: return EnumDifficulty.HARD;
        }
    }

    private static org.bukkit.Difficulty fromNMS(EnumDifficulty d) {
        switch (d) {
            case PEACEFUL: return org.bukkit.Difficulty.PEACEFUL;
            default: return org.bukkit.Difficulty.EASY;
            case NORMAL: return org.bukkit.Difficulty.NORMAL;
            case HARD: return org.bukkit.Difficulty.HARD;
        }
    }

    private static EntityCreature toNMS(Creature c) { return ((CraftCreature) c).getHandle();}

    private PathfinderGoal.Type toNMS(Pathfinder.PathfinderFlag f) {
        switch (f) {
            default: return PathfinderGoal.Type.MOVE;
            case JUMPING: return PathfinderGoal.Type.JUMP;
            case TARGETING: return PathfinderGoal.Type.TARGET;
            case LOOKING: return PathfinderGoal.Type.LOOK;
        }
    }

    private static Pathfinder.PathfinderFlag fromNMS(PathfinderGoal.Type f) {
        switch (f) {
            default: return Pathfinder.PathfinderFlag.MOVEMENT;
            case JUMP: return Pathfinder.PathfinderFlag.JUMPING;
            case TARGET: return Pathfinder.PathfinderFlag.TARGETING;
            case LOOK: return Pathfinder.PathfinderFlag.LOOKING;
        }
    }

    private static float getFloat(PathfinderGoal o, String name) { return getObject(o, name, Float.class); }

    private static double getDouble(PathfinderGoal o, String name) { return getObject(o, name, Double.class); }

    private static boolean getBoolean(PathfinderGoal o, String name) { return getObject(o, name, Boolean.class); }

    private static int getInt(PathfinderGoal o, String name) { return getObject(o, name, Integer.class); }

    private static <T> T getObject(PathfinderGoal o, String name, Class<T> cast) {
        try {
            Class<? extends PathfinderGoal> clazz = o.getClass();

            while (clazz.getSuperclass() != null) {
                try {
                    Field f = clazz.getDeclaredField(name);
                    f.setAccessible(true);
                    return cast.cast(f.get(o));
                } catch (NoSuchFieldException e) {
                    if (PathfinderGoal.class.isAssignableFrom(clazz.getSuperclass())) clazz = (Class<? extends PathfinderGoal>) clazz.getSuperclass();
                    else break;
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) Bukkit.getLogger().severe(s.toString());
        }

        return null;
    }

    private static Mob fromNMS(EntityInsentient m) { return (Mob) m.getBukkitEntity(); }

    private static World fromNMS(net.minecraft.server.v1_16_R3.World l) { return l.getWorld(); }

    private static WorldServer toNMS(World w) { return ((CraftWorld) w).getHandle(); }

    private static BlockPosition toNMS(Location l) { return new BlockPosition(l.getX(), l.getY(), l.getZ()); }

    private static List<ItemStack> fromNMS(RecipeItemStack in) { return Arrays.stream(in.choices).map(CraftItemStack::asBukkitCopy).collect(Collectors.toList()); }

    private static Sound fromNMS(SoundEffect s) { return CraftSound.getBukkit(s); }

    private static Mob getEntity(PathfinderGoal g) {
        try {
            Class<? extends PathfinderGoal> clazz = g.getClass();

            while (clazz.getSuperclass() != null) {
                for (Field f : clazz.getDeclaredFields()) {
                    f.setAccessible(true);
                    if (EntityInsentient.class.isAssignableFrom(f.getType()) && Modifier.isFinal(f.getModifiers())) {
                        return fromNMS((EntityInsentient) f.get(g));
                    }
                }

                if (PathfinderGoal.class.isAssignableFrom(clazz.getSuperclass())) clazz = (Class<? extends PathfinderGoal>) clazz.getSuperclass();
                else break;
            }

            return null;
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) Bukkit.getLogger().severe(s.toString());
            return null;
        }
    }

    private static Object invoke(PathfinderGoal g, String method, Object... args) {
        try {
            Method m = g.getClass().getDeclaredMethod(method);
            m.setAccessible(true);

            return m.invoke(g, args);
        } catch (Exception e) {
            Bukkit.getLogger().severe(e.getMessage());
            for (StackTraceElement s : e.getStackTrace()) Bukkit.getLogger().severe(s.toString());
            return null;
        }
    }

    private static CustomPathfinder custom(PathfinderGoal g) {
        return new CustomPathfinder(getEntity(g)) {
            @Override
            public @NotNull PathfinderFlag[] getFlags() {
                PathfinderFlag[] flags = new PathfinderFlag[g.i().size()];
                int i = 0;
                for (PathfinderGoal.Type f : g.i()) {
                    flags[i] = fromNMS(f);
                    i++;
                }
                return flags;
            }

            @Override
            public boolean canStart() {
                return g.a();
            }

            @Override
            public void start() {
                g.c();
            }

            @Override
            public void tick() {
                g.e();
            }

            @Override
            public String getInternalName() {
                return g.getClass().getSimpleName();
            }
        };
    }

    private static BlockPosition getPosWithBlock(net.minecraft.server.v1_16_R3.Block block, BlockPosition bp, IBlockAccess g) {
        if (g.getType(bp).a(block)) return bp;
        else {
            BlockPosition[] bp1 = new BlockPosition[]{new BlockPosition(bp.down()),  bp.west(), bp.east(), bp.north(), bp.south(), new BlockPosition(bp.up())};
            for (BlockPosition bps : bp1) if (g.getType(bps).a(block)) return bps;
            return null;
        }
    }

    private static Location fromNMS(BlockPosition p, World w) { return new Location(w, p.getX(), p.getY(), p.getZ()); }

    private Pathfinder fromNMS(PathfinderGoal g) {
        Mob m = getEntity(g);
        String name = g.getClass().getSimpleName();

        if (name.startsWith("PathfinderGoal")) {
            name = name.replace("PathfinderGoal", "");

            switch (name) {
                case "AvoidTarget": return new PathfinderAvoidEntity<>((Creature) m, fromNMS(getObject(g, "f", Class.class), LivingEntity.class), getFloat(g, "c"), getDouble(g, "i"), getDouble(g, "j"));
                case "ArrowAttack": return new PathfinderRangedAttack(m, getDouble(g, "e"), getFloat(g, "i"), getInt(g, "g"), getInt(g, "h"));
                case "Beg": return new PathfinderBeg((Wolf) m, getFloat(g, "d"));
                case "BowShoot": return new PathfinderRangedBowAttack(m, getDouble(g, "b"), (float) Math.sqrt(getFloat(g, "d")), getInt(g, "c"));
                case "BreakDoor": return new PathfinderBreakDoor(m, getInt(g, "c"), d -> getObject(g, "g", Predicate.class).test(toNMS(d)));
                case "Breath": return new PathfinderBreathAir((Creature) m);
                case "Breed": return new PathfinderBreed((Animals) m, getDouble(g, "g"));
                case "CatSitOnBed": return new PathfinderCatOnBed((Cat) m, getDouble(g, "b"), getInt(g, "i"));
                case "CrossbowAttack": return new PathfinderRangedCrossbowAttack((Pillager) m, getDouble(g, "c"), (float) Math.sqrt(getFloat(g, "d")));
                case "DoorOpen": return new PathfinderOpenDoor(m, getBoolean(g, "a"));
                case "WaterJump": return new PathfinderDolphinJump((Dolphin) m, getInt(g, "c"));
                case "EatTile": return new PathfinderEatTile(m);
                case "Water": return new PathfinderFindWater((Creature) m);
                case "FishSchool": return new PathfinderFollowFishLeader((Fish) m);
                case "FleeSun": return new PathfinderFleeSun((Creature) m, getDouble(g, "e"));
                case "Float": return new PathfinderFloat(m);
                case "FollowBoat": return new PathfinderFollowBoat((Creature) m);
                case "FollowEntity": return new PathfinderFollowMob(m, getDouble(g, "d"), getFloat(g, "g"), getFloat(g, "i"));
                case "FollowOwner": return new PathfinderFollowOwner((Tameable) m, getDouble(g, "d"), getFloat(g, "h"), getFloat(g, "g"));
                case "FollowParent": return new PathfinderFollowParent((Animals) m, getDouble(g, "c"));
                case "HorseTrap": return new PathfinderSkeletonTrap((SkeletonHorse) m);
                case "LeapAtTarget": return new PathfinderLeapAtTarget(m, getFloat(g, "c"));
                case "JumpOnBlock": return new PathfinderCatOnBlock((Cat) m, getDouble(g, "g"));
                case "LlamaFollow": return new PathfinderLlamaFollowCaravan((Llama) m, getDouble(g, "b"));
                case "LookAtPlayer": return new PathfinderLookAtEntity<>(m, fromNMS(getObject(g, "d", Class.class), LivingEntity.class), getFloat(g, "c"), getFloat(g, "g"));
                case "LookAtTradingPlayer": return new PathfinderLookAtTradingPlayer((AbstractVillager) m);
                case "MeleeAttack": return new PathfinderMeleeAttack((Creature) m, getDouble(g, "d"), getBoolean(g, "e"));
                case "MoveThroughVillage": return new PathfinderMoveThroughVillage((Creature) m, getBoolean(g, "e"), getDouble(g, "b"), getInt(g, "g"), getBoolean(g, "e"));
                case "NearestVillage": return new PathfinderRandomStrollThroughVillage((Creature) m, getInt(g, "b"));
                case "GotoTarget": return new PathfinderMoveToBlock((Creature) m, l -> fromNMS(getObject(g, "e", BlockPosition.class), m.getWorld()).equals(l), getDouble(g, "b"), getInt(g, "i"), getInt(g, "j"));
                case "Raid": return new PathfinderMoveToRaid((Raider) m);
                case "MoveTowardsRestriction": return new PathfinderMoveTowardsRestriction((Creature) m, getDouble(g, "e"));
                case "MoveTowardsTarget": return new PathfinderMoveTowardsTarget((Creature) m, getDouble(g, "f"), getFloat(g, "g"));
                case "OcelotAttack": return new PathfinderOcelotAttack((Ocelot) m);
                case "OfferFlower": return new PathfinderOfferFlower((IronGolem) m);
                case "Panic": return new PathfinderPanic((Creature) m, getDouble(g, "b"));
                case "Perch": return new PathfinderRideShoulder((Parrot) m);
                case "RandomLookaround": return new PathfinderRandomLook(m);
                case "RandomStroll": return new PathfinderRandomStroll((Creature) m, getDouble(g, "e"), getInt(g, "f"));
                case "RandomStrollLand": return new PathfinderRandomStrollLand((Creature) m, getDouble(g, "e"), getFloat(g, "h"));
                case "RandomSwim": return new PathfinderRandomSwim((Creature) m, getDouble(g, "e"), getInt(g, "f"));
                case "RandomFly": return new PathfinderRandomStrollFlying((Creature) m, getDouble(g, "e"));
                case "RemoveBlock": return new PathfinderRemoveBlock((Creature) m, m.getWorld().getBlockAt(fromNMS(getPosWithBlock(getObject(g, "g", Block.class), toNMS(m.getLocation()), toNMS(m.getWorld())), m.getWorld())), getDouble(g, "b"));
                case "RestrictSun": return new PathfinderRestrictSun((Creature) m);
                case "Sit": return new PathfinderSit((Tameable) m);
                case "StrollVillage": return new PathfinderRandomStrollToVillage((Creature) m, getDouble(g, "e"));
                case "Swell": return new PathfinderSwellCreeper((Creeper) m);
                case "Tame": return new PathfinderTameHorse((AbstractHorse) m);
                case "Tempt": return new PathfinderTempt((Creature) m, getDouble(g, "e"), fromNMS(getObject(g, "l", RecipeItemStack.class)));
                case "TradeWithPlayer": return new PathfinderTradePlayer((AbstractVillager) m);
                case "UseItem": return new PathfinderUseItem(m, fromNMS(getObject(g, "b", net.minecraft.server.v1_16_R3.ItemStack.class)), en -> getObject(g, "c", Predicate.class).test(toNMS(en)), fromNMS(getObject(g, "d", SoundEffect.class)));
                case "ZombieAttack": return new PathfinderZombieAttack((Zombie) m, getDouble(g, "d"), getBoolean(g, "e"));

                // Target
                case "NearestAttackableTarget": return new PathfinderNearestAttackableTarget<>(m, fromNMS(getObject(g, "a", Class.class), LivingEntity.class), getInt(g, "b"), true, true);
                case "NearestAttackableTargetWitch": return new PathfinderNearestAttackableTargetRaider<>((Raider) m, fromNMS(getObject(g, "a", Class.class), LivingEntity.class), getInt(g, "b"), true, true, l -> getObject(g, "d", PathfinderTargetCondition.class).a(null, toNMS(l)));
                case "NearestHealableRaider": return new PathfinderNearestHealableRaider<>((Raider) m, fromNMS(getObject(g, "a", Class.class), LivingEntity.class), true,  l -> getObject(g, "d", PathfinderTargetCondition.class).a(null, toNMS(l)));
                case "DefendVillage": return new PathfinderDefendVillage((IronGolem) m);
                case "HurtByTarget": return new PathfinderHurtByTarget((Creature) m, getEntityTypes(getObject(g, "d", Class[].class)));
                case "OwnerHurtByTarget": return new PathfinderOwnerHurtByTarget((Tameable) m);
                case "OwnerHurtTarget": return new PathfinderOwnerHurtTarget((Tameable) m);

                default: return custom(g);
            }
        } else return custom(g);
    }

    private static class Attribute1_16_R3 extends AttributeRanged implements Attribute {

        private final NamespacedKey key;
        private final double defaultV;
        private final double min;
        private final double max;

        private static double getDouble(AttributeRanged r, String s) {
            try {
                Field f = r.getClass().getDeclaredField(s);
                f.setAccessible(true);
                return f.getDouble(r);
            } catch (Exception e) {
                return 0;
            }
        }

        public Attribute1_16_R3(AttributeRanged a) {
            super(a.getName(), a.getDefault(), getDouble(a, "a"), a.maximum);
            this.key = IRegistry.ATTRIBUTE.getKey(a) == null ? NamespacedKey.minecraft(a.getName()) : CraftNamespacedKey.fromMinecraft(IRegistry.ATTRIBUTE.getKey(a));
            this.defaultV = a.getDefault();
            this.min = getDouble(a, "a");
            this.max = a.maximum;
        }

        public Attribute1_16_R3(NamespacedKey key, double defaultV, double min, double max, boolean clientSide) {
            super("attribute.name." +  key.getKey().toLowerCase(), defaultV, min, max);
            this.key = key;
            this.min = min;
            this.defaultV = defaultV;
            this.max = max;
            this.a(clientSide);
        }

        public double getMinValue() {
            return this.min;
        }

        public double getDefaultValue() {
            return this.defaultV;
        }

        public double getMaxValue() {
            return this.max;
        }

        @Override
        public boolean isClientSide() {
            return b();
        }

        @NotNull
        @Override
        public NamespacedKey getKey() {
            return this.key;
        }
    }

    private static class AttributeInstance1_16_R3 implements AttributeInstance {

        private final AttributeModifiable handle;
        private final Attribute a;

        AttributeInstance1_16_R3(Attribute a, AttributeModifiable handle) {
            this.a = a;
            this.handle = handle;
        }

        @Override
        public @NotNull Attribute getGenericAttribute() {
            return this.a;
        }

        @Override
        public double getBaseValue() {
            return handle.getBaseValue();
        }

        @Override
        public void setBaseValue(double v) {
            handle.setValue(v);
        }

        @NotNull
        @Override
        public Collection<org.bukkit.attribute.AttributeModifier> getModifiers() {
            return handle.getModifiers().stream().map(CraftAttributeInstance::convert).collect(Collectors.toSet());
        }

        @Override
        public void addModifier(@NotNull org.bukkit.attribute.AttributeModifier mod) {
            Preconditions.checkArgument(mod != null, "modifier");
            handle.addModifier(CraftAttributeInstance.convert(mod));
        }

        @Override
        public void removeModifier(@NotNull AttributeModifier mod) {
            Preconditions.checkArgument(mod != null, "modifier");
            handle.removeModifier(CraftAttributeInstance.convert(mod));
        }

        @Override
        public double getValue() {
            return handle.getValue();
        }

        @Override
        public double getDefaultValue() {
            return handle.getAttribute().getDefault();
        }
    }

    @Override
    public Attribute registerAttribute(NamespacedKey key, double defaultV, double min, double max, boolean client) {
        if (existsAttribute(key)) return null;

        DedicatedServer server = ((CraftServer) Bukkit.getServer()).getServer();
        IRegistryWritable<AttributeBase> writable = server.getCustomRegistry().b(IRegistry.y);
        ResourceKey<AttributeBase> nmsKey = ResourceKey.a(IRegistry.y, toNMS(key));
        Attribute1_16_R3 att = new Attribute1_16_R3(key, defaultV, min, max, client);
        writable.a(nmsKey, att, Lifecycle.stable());
        return att;
    }

    @Override
    public boolean existsAttribute(NamespacedKey key) {
        try {
            DedicatedServer server = ((CraftServer) Bukkit.getServer()).getServer();
            RegistryMaterials<AttributeBase> registry = (RegistryMaterials<AttributeBase>) server.getCustomRegistry().b(IRegistry.y);

            Field res = RegistryMaterials.class.getDeclaredField("bh");
            res.setAccessible(true);
            Map<MinecraftKey, AttributeBase> map = (Map<MinecraftKey, AttributeBase>) res.get(registry);
            return map.containsKey(toNMS(key));
        } catch (Exception e) {
            return false;
        }
    }

    private static MinecraftKey toNMS(NamespacedKey key) {
        return CraftNamespacedKey.toMinecraft(key);
    }

    @Override
    public Attribute getAttribute(NamespacedKey key) {
        AttributeBase a = IRegistry.ATTRIBUTE.get(toNMS(key));
        if (!(a instanceof AttributeRanged)) return null;
        return new Attribute1_16_R3((AttributeRanged) a);
    }

    @Override
    public AttributeInstance getAttributeInstance(Mob m, Attribute a) {
        AttributeBase nmsAttribute = IRegistry.ATTRIBUTE.get(toNMS(a.getKey()));
        return new AttributeInstance1_16_R3(a, toNMS(m).getAttributeInstance(nmsAttribute));
    }

    private static ReputationType toNMS(GossipType t) {
        return ReputationType.a(t.getKey().getKey());
    }

    private static GossipType fromNMS(ReputationType t) {
        return GossipType.getByKey(NamespacedKey.minecraft(t.f));
    }

    private static class EntityGossipContainer1_16_R3 implements EntityGossipContainer {
        private final Reputation handle;

        EntityGossipContainer1_16_R3(Villager v) {
            this.handle = ((CraftVillager) v).getHandle().fj();
        }

        @Override
        public void decay() {
            handle.b();
        }

        @Override
        public int getReputation(@NotNull Entity en, @Nullable GossipType... types) throws IllegalArgumentException {
            return handle.a(en.getUniqueId(), g -> Arrays.asList(types).contains(fromNMS(g)));
        }

        @Override
        public void put(@NotNull Entity en, @NotNull GossipType type, int maxCap) throws IllegalArgumentException {
            handle.a(en.getUniqueId(), toNMS(type), maxCap);
        }

        @Override
        public void remove(@NotNull Entity en, @NotNull GossipType type) throws IllegalArgumentException {
            try {
                Field map = Reputation.class.getDeclaredField("a");
                map.setAccessible(true);
                Map<UUID, ?> data = new HashMap<>((Map<UUID, ?>) map.get(handle));
                data.remove(en.getUniqueId());
                map.set(handle, data);
            } catch (Exception e) {
                Bukkit.getLogger().severe(e.getClass().getSimpleName());
                Bukkit.getLogger().severe(e.getMessage());
                for (StackTraceElement s : e.getStackTrace()) Bukkit.getLogger().severe(s.toString());
            }
        }

        @Override
        public void removeAll(@NotNull GossipType type) throws IllegalArgumentException {
            try {
                Field map = Reputation.class.getDeclaredField("a");
                map.setAccessible(true);
                Map<UUID, ?> data = new HashMap<>((Map<UUID, ?>) map.get(handle));

                for (UUID uuid : data.keySet()) {
                    Object o = data.get(uuid);

                    Method m = o.getClass().getDeclaredMethod("b", ReputationType.class);
                    m.setAccessible(true);
                    m.invoke(o, toNMS(type));

                    Method empty = o.getClass().getDeclaredMethod("b");
                    empty.setAccessible(true);
                    if ((boolean) empty.invoke(o)) data.remove(uuid);
                }
                map.set(handle, data);
            } catch (Exception e) {
                Bukkit.getLogger().severe(e.getClass().getSimpleName());
                Bukkit.getLogger().severe(e.getMessage());
                for (StackTraceElement s : e.getStackTrace()) Bukkit.getLogger().severe(s.toString());
            }
        }
    }

    @Override
    public EntityGossipContainer getGossipContainer(Villager v) {
        return new EntityGossipContainer1_16_R3(v);
    }
    
}
