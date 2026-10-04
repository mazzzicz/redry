package cz.redry.nightfall.world;

import cz.redry.nightfall.RedryMod;
import cz.redry.nightfall.audio.RedrySounds;
import cz.redry.nightfall.entity.HerobrineEntity;
import cz.redry.nightfall.entity.RedryAnomalyEntity;
import cz.redry.nightfall.entity.RedryEntities;
import cz.redry.nightfall.entity.SignalEchoEntity;
import cz.redry.nightfall.entity.StillOneEntity;
import cz.redry.nightfall.item.RedryItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LightLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-owned pacing, persistent-in-session fear, distant sightings, and night escalation. */
public final class NightDirector {
    private static final Map<UUID, PlayerState> PLAYERS = new HashMap<>();

    static {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> PLAYERS.clear());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> PLAYERS.clear());
    }

    private NightDirector() {
    }

    public static boolean isNight(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD) {
            return false;
        }
        long time = Math.floorMod(level.getDayTime(), 24_000L);
        return time >= 13_000L && time <= 23_000L;
    }

    public static void relieveFear(ServerPlayer player, int amount) {
        PlayerState state = PLAYERS.get(player.getUUID());
        if (state != null) {
            state.fear = Math.max(0, state.fear - Math.max(0, amount));
        }
    }

    public static int fearOf(UUID playerId) {
        PlayerState state = PLAYERS.get(playerId);
        return state == null ? 0 : state.fear;
    }

    public static void tick(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD || level.getGameTime() % 20L != 0L) {
            return;
        }

        long now = level.getGameTime();
        long nightNumber = Math.max(1L, Math.floorDiv(level.getDayTime(), 24_000L) + 1L);
        boolean night = isNight(level);
        boolean peaceful = level.getDifficulty() == Difficulty.PEACEFUL;

        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isCreative() || player.isSpectator()) {
                PlayerState existing = PLAYERS.get(player.getUUID());
                if (existing != null) {
                    existing.fear = Math.max(0, existing.fear - 3);
                }
                continue;
            }

            PlayerState state = PLAYERS.computeIfAbsent(player.getUUID(), ignored -> new PlayerState(now, level));
            if (peaceful) {
                state.fear = 0;
                continue;
            }

            if (!night) {
                state.fear = Math.max(0, state.fear - 2);
                // Never bank a surprise spawn for sunrise: the next night starts with a warning window.
                state.nextEncounter = Math.max(state.nextEncounter, now + 800L);
                continue;
            }

            updateFear(level, player, state, now);
            scheduleWhisper(level, player, state, now);
            scheduleFearSpike(level, player, state, now);
            scheduleEncounter(level, player, state, now, nightNumber);
        }
    }

    private static void updateFear(ServerLevel level, ServerPlayer player, PlayerState state, long now) {
        int blockLight = level.getBrightness(LightLayer.BLOCK, player.blockPosition());
        boolean heldReceiver = player.getMainHandItem().is(RedryItems.SIGNAL_RECEIVER)
                || player.getOffhandItem().is(RedryItems.SIGNAL_RECEIVER);
        boolean anomalyNearby = !level.getEntitiesOfClass(
                RedryAnomalyEntity.class,
                player.getBoundingBox().inflate(32.0D),
                RedryAnomalyEntity::isAlive
        ).isEmpty();

        if (now % 80L == 0L) {
            state.fear++;
        }
        if (blockLight <= 3 && now % 40L == 0L) {
            state.fear += 2;
        } else if (blockLight <= 7 && now % 80L == 0L) {
            state.fear++;
        }
        if (anomalyNearby && now % 40L == 0L) {
            state.fear += 2;
        }
        if (player.getHealth() < player.getMaxHealth() * 0.3F && now % 100L == 0L) {
            state.fear++;
        }
        if (heldReceiver && now % 40L == 0L) {
            state.fear -= 2;
        } else if (blockLight >= 11 && !anomalyNearby && now % 100L == 0L) {
            state.fear--;
        }
        state.fear = Math.max(0, Math.min(100, state.fear));
    }

    private static void scheduleWhisper(ServerLevel level, ServerPlayer player, PlayerState state, long now) {
        if (state.fear < 12 || now < state.nextWhisper) {
            return;
        }

        String[] lines = {
                "§8Za tebou někdo přestal dýchat.",
                "§8Signál právě vyslovil tvoje jméno.",
                "§8Chat je prázdný. Proč bliká počítadlo diváků?",
                "§8Ten krok nebyl ozvěna.",
                "§8Nedívej se do inventáře. Už tam někdo je."
        };
        BlockPos behind = player.blockPosition()
                .relative(player.getDirection().getOpposite(), 4)
                .above();
        float pitch = 0.54F + level.getRandom().nextFloat() * 0.36F;
        level.playSound(null, behind, RedrySounds.WHISPER,
                SoundSource.AMBIENT, 0.40F + state.fear / 240.0F, pitch);

        if (level.getRandom().nextFloat() < 0.18F + state.fear / 180.0F) {
            player.sendSystemMessage(Component.literal(lines[level.getRandom().nextInt(lines.length)]), true);
        }
        if (state.fear > 52 && level.getRandom().nextFloat() < 0.22F) {
            level.playSound(null, player.blockPosition().relative(player.getDirection().getOpposite(), 7),
                    RedrySounds.DISTANT_KNOCK, SoundSource.AMBIENT, 0.62F, 0.48F);
        }

        state.nextWhisper = now + 440L + level.getRandom().nextInt(620);
    }

    private static void scheduleFearSpike(ServerLevel level, ServerPlayer player, PlayerState state, long now) {
        if (state.fear >= 72 && now >= state.nextShock) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.DARKNESS, 30, 0, true, false, false));
            level.playSound(null, player.blockPosition(), RedrySounds.HEARTBEAT,
                    SoundSource.AMBIENT, 0.66F + state.fear / 300.0F, 0.58F);
            player.sendSystemMessage(Component.literal("§4ZÁZNAM PŘESKOČIL."), true);
            state.nextShock = now + 1_100L + level.getRandom().nextInt(1_300);
        }

        if (state.fear >= 91 && now >= state.nextGlitch) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.BLINDNESS, 14, 0, true, false, false));
            level.playSound(null, player.blockPosition(), RedrySounds.STATIC,
                    SoundSource.AMBIENT, 0.78F, 0.54F);
            player.sendSystemMessage(Component.literal("§5[ARCHIV] §k████████ §r§5záznam pokračuje"), true);
            state.nextGlitch = now + 1_900L + level.getRandom().nextInt(1_800);
        }
    }

    private static void scheduleEncounter(ServerLevel level, ServerPlayer player, PlayerState state,
                                          long now, long nightNumber) {
        if (now < state.nextEncounter) {
            return;
        }

        if (!level.getEntitiesOfClass(RedryAnomalyEntity.class,
                player.getBoundingBox().inflate(76.0D)).isEmpty()) {
            state.nextEncounter = now + 520L;
            return;
        }

        BlockPos spawnPos = findDistantPosition(level, player);
        if (spawnPos == null) {
            state.nextEncounter = now + 420L;
            return;
        }

        RedryAnomalyEntity anomaly;
        if (nightNumber >= 4L && state.fear >= 34 && level.getRandom().nextFloat() < 0.34F) {
            anomaly = new SignalEchoEntity(RedryEntities.CHAT_ECHO, level);
            player.sendSystemMessage(Component.literal("§5[CHAT] §dneznámý divák vstoupil do světa"), true);
            level.playSound(null, player.blockPosition(), RedrySounds.STATIC,
                    SoundSource.AMBIENT, 0.72F, 0.64F);
        } else if (nightNumber >= 2L && player.blockPosition().getY() < 58
                && state.fear >= 20 && level.getRandom().nextFloat() < 0.44F) {
            anomaly = new StillOneEntity(RedryEntities.STILL_ONE, level);
            player.sendSystemMessage(Component.literal("§8Někdo za tebou má stejný počet kloubů."), true);
            level.playSound(null, player.blockPosition(), RedrySounds.SIGNAL,
                    SoundSource.AMBIENT, 0.78F, 0.58F);
        } else {
            HerobrineEntity herobrine = new HerobrineEntity(RedryEntities.HEROBRINE, level);
            herobrine.beginDistantAppearance(64);
            anomaly = herobrine;
            player.sendSystemMessage(Component.literal("§7Na okraji světla stojí někdo, kdo tu nemá být."), true);
            level.playSound(null, player.blockPosition(), RedrySounds.WHISPER,
                    SoundSource.AMBIENT, 0.76F, 0.62F);
        }

        double dxToPlayer = player.getX() - (spawnPos.getX() + 0.5D);
        double dzToPlayer = player.getZ() - (spawnPos.getZ() + 0.5D);
        float yaw = (float) (Math.atan2(dzToPlayer, dxToPlayer) * 180.0D / Math.PI) - 90.0F;
        anomaly.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D, yaw, 0.0F);
        if (!level.addFreshEntity(anomaly)) {
            anomaly.discard();
            state.nextEncounter = now + 400L;
            return;
        }

        long calmWindow = Math.max(2_800L, 5_500L - Math.min(nightNumber, 12L) * 210L);
        state.nextEncounter = now + calmWindow + level.getRandom().nextInt(1_800);
        RedryMod.LOGGER.debug("Noční archivní setkání poblíž {} (noc {}, strach {}).",
                player.getGameProfile().getName(), nightNumber, state.fear);
    }

    private static BlockPos findDistantPosition(ServerLevel level, Player player) {
        double backAngle = Math.atan2(player.getViewVector(1.0F).z, player.getViewVector(1.0F).x) + Math.PI;
        int baseY = player.blockPosition().getY();

        for (int attempt = 0; attempt < 24; attempt++) {
            double angle = backAngle + (level.getRandom().nextDouble() - 0.5D) * 1.25D;
            int distance = 20 + level.getRandom().nextInt(15);
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * distance);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * distance);

            for (int dy = -6; dy <= 6; dy++) {
                BlockPos feet = new BlockPos(x, baseY + dy, z);
                BlockState atFeet = level.getBlockState(feet);
                BlockState atHead = level.getBlockState(feet.above());
                BlockState below = level.getBlockState(feet.below());
                if (atFeet.isAir() && atHead.isAir() && !below.isAir()
                        && level.getFluidState(feet).isEmpty()
                        && below.isFaceSturdy(level, feet.below(), Direction.UP)) {
                    return feet;
                }
            }
        }
        return null;
    }

    private static final class PlayerState {
        private int fear;
        private long nextWhisper;
        private long nextEncounter;
        private long nextShock;
        private long nextGlitch;

        private PlayerState(long now, ServerLevel level) {
            this.nextWhisper = now + 180L + level.getRandom().nextInt(240);
            this.nextEncounter = now + 2_200L + level.getRandom().nextInt(1_300);
            this.nextShock = now + 900L + level.getRandom().nextInt(900);
            this.nextGlitch = now + 1_700L + level.getRandom().nextInt(1_300);
        }
    }
}
