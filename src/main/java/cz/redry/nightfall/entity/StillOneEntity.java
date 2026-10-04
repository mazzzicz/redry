package cz.redry.nightfall.entity;

import cz.redry.nightfall.audio.RedrySounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** RED-173, the Still One: every living witness must lose sight before it can advance. */
public final class StillOneEntity extends RedryAnomalyEntity {
    private int lastNearMissTick = -1_000;
    private int unobservedTicks;

    public StillOneEntity(EntityType<? extends StillOneEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createAnomalyAttributes()
                .add(Attributes.MAX_HEALTH, 78.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.38D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 46.0D);
    }

    public float horrorAnimationPhase(float partialTick) {
        return this.tickCount + partialTick;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() || !this.isAlive() || !(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        List<Player> observers = this.level().getEntitiesOfClass(
                Player.class,
                this.getBoundingBox().inflate(34.0D),
                player -> player.isAlive() && !player.isCreative() && !player.isSpectator()
        );
        Player nearest = observers.stream()
                .min((first, second) -> Double.compare(this.distanceToSqr(first), this.distanceToSqr(second)))
                .orElse(null);
        boolean watched = observers.stream().anyMatch(this::isLookingAt);

        this.setNoAi(watched);
        if (watched) {
            this.unobservedTicks = 0;
            this.getNavigation().stop();
            if (nearest != null && this.distanceToSqr(nearest) < 11.0D * 11.0D
                    && this.tickCount - this.lastNearMissTick > 120) {
                this.lastNearMissTick = this.tickCount;
                nearest.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 32, 0, true, false, true));
                serverLevel.playSound(null, nearest.blockPosition(), RedrySounds.HEARTBEAT,
                        SoundSource.AMBIENT, 0.72F, 0.54F);
            }
        } else {
            this.unobservedTicks++;
            // Once nobody can see it, the statue stops pretending to be heavy stone.
            if (this.unobservedTicks > 20 && nearest != null) {
                this.getNavigation().moveTo(nearest, this.unobservedTicks > 80 ? 1.3D : 0.95D);
            }
        }
    }

    private boolean isLookingAt(Player player) {
        if (this.distanceToSqr(player) > 34.0D * 34.0D || !player.hasLineOfSight(this)) {
            return false;
        }
        Vec3 towardEntity = this.position().subtract(player.getEyePosition()).normalize();
        return player.getViewVector(1.0F).dot(towardEntity) > 0.925D;
    }
}
